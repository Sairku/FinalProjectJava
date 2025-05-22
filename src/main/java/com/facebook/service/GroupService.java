package com.facebook.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.facebook.dto.*;
import com.facebook.enums.GroupJoinStatus;
import com.facebook.enums.GroupRole;
import com.facebook.exception.NotFoundException;
import com.facebook.model.Group;
import com.facebook.model.GroupJoinRequest;
import com.facebook.model.GroupMember;
import com.facebook.model.User;
import com.facebook.repository.GroupJoinRequestRepository;
import com.facebook.repository.GroupMemberRepository;
import com.facebook.repository.GroupRepository;
import com.facebook.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class GroupService {
    private final GroupRepository groupRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupJoinRequestRepository groupJoinRequestRepository;

    public GroupResponse create(GroupCreateRequest groupCreateRequest) {
        long ownerId = groupCreateRequest.getOwnerId();
        modelMapper.typeMap(GroupCreateRequest.class, Group.class)
                .addMappings(m -> m.skip(Group::setId));

        Group maybeGroup = modelMapper.map(groupCreateRequest, Group.class);
        User maybeUser = userRepository.findById(ownerId)
                .orElseThrow(()->new NotFoundException("No user with ID: "+ ownerId));

        maybeGroup.setOwner(maybeUser);
        Group groupSaved = groupRepository.save(maybeGroup);

        GroupMember groupMember = new GroupMember();
        groupMember.setGroup(groupSaved);
        groupMember.setUser(maybeUser);
        groupMember.setRole(GroupRole.ADMIN);

        groupMemberRepository.save(groupMember);

        return modelMapper.map(groupSaved, GroupResponse.class);
    }

    public GroupResponse update(Long groupId, GroupUpdateRequest updateRequest) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        if (updateRequest.getDescription() != null)
            group.setDescription(updateRequest.getDescription());


        if (updateRequest.getImgUrl() != null)
            group.setImageUrl(updateRequest.getImgUrl());

        if (updateRequest.getColor() != null)
            group.setColor(updateRequest.getColor());

        groupRepository.save(group);

        return modelMapper.map(group, GroupResponse.class);
    }


    public void delete(Long id, GroupDeleteRequest deleteRequest) {
        Optional<Group> maybeGroup = groupRepository.findById(id);

        if (maybeGroup.isEmpty())
            throw new NotFoundException("Group with id "+id+" not found");
        else if(!Objects.equals(maybeGroup.get().getOwner().getId(), deleteRequest.getOwnerId()))
            throw new RuntimeException("You are not owner of this group");

        groupRepository.deleteById(id);
    }

    public void addUserToGroup(GroupAddRequest addRequest) {
        long groupId = addRequest.getGroupId();
        long userId = addRequest.getUserId();

        Group group = groupRepository.findById(groupId).orElseThrow(
                () -> new NotFoundException("Group with id " + groupId + " not found"));

        GroupMember maybeGroupMembers = groupMemberRepository.findByGroupIdAndUserId(groupId, userId).orElse(null);

        // If user is already a member of the group, throw an exception
        if (maybeGroupMembers != null) {
            throw new RuntimeException("User with Id " + userId + " is already a member of this group");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));

        // If the group is not private add the user directly
        if (!group.isPrivate()) {
            GroupMember newMember = new GroupMember();

            newMember.setGroup(group);
            newMember.setUser(user);
            newMember.setRole(GroupRole.MEMBER);

            groupMemberRepository.save(newMember);
            return;
        }

        // If the group is private add the user to the join request list
        Optional<GroupJoinRequest> request = groupJoinRequestRepository.findTop1ByGroup_IdAndUser_IdOrderByCreatedDateDesc(groupId, userId);

        if (request.isPresent()) {
            // If the user already has a request, update it
            GroupJoinRequest joinRequest = request.get();
            joinRequest.setStatus(GroupJoinStatus.PENDING);
            groupJoinRequestRepository.save(joinRequest);
        } else {
            // If the user doesn't have a request, create a new one
            GroupJoinRequest newRequest = new GroupJoinRequest();
            newRequest.setGroup(group);
            newRequest.setUser(user);
            newRequest.setInitiator(user);
            newRequest.setStatus(GroupJoinStatus.PENDING);

            groupJoinRequestRepository.save(newRequest);
        }
    }

    public void respondToAddingRequest(GroupRespondRequest respondRequest) {
        long groupId = respondRequest.getGroupId();
        long userId = respondRequest.getUserId();

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group with id " + groupId + " not found"));
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User with id " + userId + " not found"));

        Optional<GroupMember> alreadyMember = groupMemberRepository.findByGroupIdAndUserId(groupId, userId);

        if (alreadyMember.isPresent()){
            throw new RuntimeException("This user is already is member of the group");
        }

        GroupJoinRequest request = groupJoinRequestRepository
                .findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new NotFoundException("Join request not found"));

        if (respondRequest.getStatus().equals(GroupJoinStatus.APPROVED)) {
            GroupMember groupMember = new GroupMember();

            groupMember.setGroup(group);
            groupMember.setUser(user);
            groupMember.setRole(GroupRole.MEMBER);

            groupMemberRepository.save(groupMember);
        }

        // Delete from table group_join_requests
        groupJoinRequestRepository.delete(request);
    }

    public List<UserShortDto> getGroupMembers(long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group with id " + groupId + " not found"));
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId)
                .orElseThrow(() -> new NotFoundException("No members found for this group"));

        return members.stream()
                .map(member -> modelMapper.map(member.getUser(), UserShortDto.class))
                .toList();
    }

    public List<UserShortDto> getGroupRequest(long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group with id " + groupId + " not found"));
        List<GroupJoinRequest> requests = groupJoinRequestRepository.findByGroupId(groupId)
                .orElseThrow(() -> new NotFoundException("No requests found for this group"));

        return requests.stream()
                .map(request -> modelMapper.map(request.getUser(), UserShortDto.class))
                .toList();
    }

    public Group findById(Long id){
        return groupRepository.findById(id)
                .orElseThrow(()->new NotFoundException("Group isn't found"));
    }

    public Page<GroupResponse> getAll(Pageable pageable) {
        return groupRepository.findAll(pageable)
                .map(group -> modelMapper.map(group, GroupResponse.class));
    }
}

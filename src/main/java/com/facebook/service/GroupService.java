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
        else
            groupRepository.deleteById(id);
    }

    public void addUserToGroup(GroupAddRequest addRequest) {
        Optional<Group> maybeGroup = groupRepository.findById(addRequest.getGroupId());
        if(maybeGroup.isEmpty())
            throw new NotFoundException("Group with id "+addRequest.getGroupId()+" not found");
        else{
            Group group = maybeGroup.get();
            Optional<List<GroupMember>> maybeGroupMembers = groupMemberRepository.findByGroupId(addRequest.getGroupId());

            boolean userAlreadyInGroup = maybeGroupMembers
                    .map(list -> list.stream()
                            .anyMatch(member -> member.getUser().getId().equals(addRequest.getUserId())))
                    .orElse(false);

            if (userAlreadyInGroup) {
                throw new RuntimeException("User with Id " + addRequest.getUserId() + " is already a member of this group");
            }
            Long groupOwnerId = group.getOwner().getId();
            boolean whileCreating = (groupOwnerId.equals(addRequest.getInitiatedBy())
                    && groupOwnerId.equals(addRequest.getUserId()));
            if (!group.isPrivate() || whileCreating) {
                // Публічна група/створення групи - додаємо одразу
                GroupMember newMember = new GroupMember();
                newMember.setGroup(group);
                newMember.setUser(userRepository.findById(addRequest.getUserId()).get());
                GroupRole role = whileCreating ? GroupRole.ADMIN : GroupRole.MEMBER;
                newMember.setRole(role);
                groupMemberRepository.save(newMember);
            }
            else {
                // Приватна група — створюємо або оновлюємо заявку
                Optional<GroupJoinRequest> existingRequest = groupJoinRequestRepository
                        .findTop1ByGroup_IdAndUser_IdOrderByCreatedDateDesc(addRequest.getGroupId(), addRequest.getUserId());

                GroupJoinRequest request = existingRequest.orElseGet(GroupJoinRequest::new);
                request.setGroup(group);
                request.setUser(userRepository.findById(addRequest.getUserId()).get());
                request.setInitiator(userRepository.findById(addRequest.getInitiatedBy()).get());
                request.setStatus(GroupJoinStatus.PENDING);
                groupJoinRequestRepository.save(request);
            }
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

    public List<UserShortInfo> getGroupMembers(long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group with id " + groupId + " not found"));
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId)
                .orElseThrow(() -> new NotFoundException("No members found for this group"));
        return members.stream()
                .map(member -> modelMapper.map(member.getUser(), UserShortInfo.class))
                .toList();
    }

    public List<UserShortInfo> getGroupRequest(long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group with id " + groupId + " not found"));
        List<GroupJoinRequest> requests = groupJoinRequestRepository.findByGroupId(groupId)
                .orElseThrow(() -> new NotFoundException("No requests found for this group"));
        return requests.stream()
                .map(request -> modelMapper.map(request.getUser(), UserShortInfo.class))
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

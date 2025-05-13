package com.facebook.service;

import com.facebook.dto.GroupCreateRequest;
import com.facebook.dto.GroupResponse;
import com.facebook.dto.GroupUpdateRequest;
import com.facebook.exception.NotFoundException;
import com.facebook.model.Group;
import com.facebook.repository.GroupRepository;
import com.facebook.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class GroupService {
    private final GroupRepository groupRepository;
    private  final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final Long currentUserId = 1L;

    public GroupResponse create(GroupCreateRequest groupCreateRequest) {
        Group maybeGroup = modelMapper.map(groupCreateRequest, Group.class);

        // обов'язково треба встановлювати поточного користувача ownerом , адже тоді буде створюватися фіктивна група, із якою не можна провзаємодіяти
        maybeGroup.setOwner(userRepository.findById(currentUserId).orElseThrow(()-> new NotFoundException("No user with id: "+currentUserId)));
        return modelMapper.map(groupRepository.save(maybeGroup), GroupResponse.class);
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


    public void delete(Long id) {
        Optional<Group> maybeGroup = groupRepository.findById(id);

        if (maybeGroup.isEmpty())
            throw new NotFoundException("Group with id "+id+" not found");
        else if(maybeGroup.get().getOwner().getId() != currentUserId)
            throw new RuntimeException("You are not owner of this group");
        else
            groupRepository.deleteById(id);
    }

}

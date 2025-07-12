package com.facebook.service;

import com.facebook.dto.*;
import com.facebook.enums.GroupJoinStatus;
import com.facebook.exception.NotFoundException;
import com.facebook.model.Group;
import com.facebook.model.GroupJoinRequest;
import com.facebook.model.GroupMember;
import com.facebook.model.User;
import com.facebook.repository.GroupJoinRequestRepository;
import com.facebook.repository.GroupMemberRepository;
import com.facebook.repository.GroupRepository;
import com.facebook.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @InjectMocks
    private GroupService groupService;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private GroupJoinRequestRepository groupJoinRequestRepository;

    @Mock
    private ModelMapper realModelMapper;


    @Mock
    private UserRepository userRepository;

    private GroupCreateRequest createRequest;
    private GroupUpdateRequest updateRequest;
    private GroupMemberRequest groupMemberRequest;
    private GroupJoinRequest groupJoinRequest;
    private User owner;
    private User guest;
    private Group group;
    private final long ownerId = 1L;
    private final long guestId = 2L;
    private final long groupId = 100L;

    @BeforeEach
    void setUp() {
        MockMvcBuilders.standaloneSetup(groupService).build();
        realModelMapper = new ModelMapper();
        realModelMapper.typeMap(GroupCreateRequest.class, Group.class)
                .addMappings(mapper -> mapper.map(GroupCreateRequest::getIsPrivate, Group::setPrivate));
        ReflectionTestUtils.setField(groupService, "modelMapper", realModelMapper);
        owner = new User();
        owner.setId(ownerId);
        owner.setFirstName("Name");
        owner.setLastName("LastName");
        owner.setEmail("email@email.com");
        owner.setPassword("Password123");

        guest = new User();
        guest.setId(guestId);
        guest.setFirstName("Guest");
        guest.setLastName("LastName");
        guest.setEmail("guest@gmail.com");
        guest.setPassword("Password123");

        group = new Group();
        group.setId(groupId);
        group.setName("Test Group");
        group.setOwner(owner);
        group.setPrivate(true);

        createRequest = new GroupCreateRequest();
        createRequest.setName("New Test Group");
        createRequest.setIsPrivate(true);

        updateRequest = new GroupUpdateRequest();
        updateRequest.setDescription("Updated");
        updateRequest.setColor("#000000");
        updateRequest.setImgUrl("http://test.png");

        groupMemberRequest = new GroupMemberRequest();
        groupMemberRequest.setUserId(guestId);
        groupMemberRequest.setStatus(GroupJoinStatus.APPROVED.name());

        groupJoinRequest = new GroupJoinRequest();
        groupJoinRequest.setGroup(group);
        groupJoinRequest.setUser(guest);
        groupJoinRequest.setStatus(GroupJoinStatus.PENDING);
    }

    @Test
    void create_shouldSaveGroup() {
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(groupRepository.save(any(Group.class))).thenAnswer(i -> i.getArgument(0));
        GroupResponse response = groupService.create(ownerId, createRequest);
        assertEquals(createRequest.getName(), response.getName());
        assertTrue(response.isPrivate());
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    @Test
    void update_shouldUpdateGroupFields() {
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupRepository.save(any(Group.class))).thenAnswer(i -> i.getArgument(0));
        groupService.update(groupId, updateRequest, group.getOwner().getId());
        assertEquals("Updated", group.getDescription());
        assertEquals("#000000", group.getColor());
        assertEquals("http://test.png", group.getImageUrl());
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    @Test
    void delete_shouldDeleteGroup() {
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        groupService.delete(groupId, group.getOwner().getId());
        verify(groupRepository).deleteById(groupId);
    }

    @Test
    void addUserToGroup_shouldHandlePrivateGroup() {
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(guestId)).thenReturn(Optional.of(guest));
        when(groupJoinRequestRepository.findTop1ByGroup_IdAndUser_IdOrderByCreatedDateDesc(groupId, guestId))
                .thenReturn(Optional.empty());
        groupService.addUserToGroup(groupId, guestId, guestId);
        verify(groupJoinRequestRepository, times(1)).save(any(GroupJoinRequest.class));
    }


    @Test
    void respondToAddingRequest_shouldApproveUser() {
        groupMemberRequest.setStatus(GroupJoinStatus.APPROVED.name());
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(guestId)).thenReturn(Optional.of(guest));
        when(groupJoinRequestRepository.findByGroupIdAndUserId(groupId, guestId)).thenReturn(Optional.of(groupJoinRequest));
        when(groupMemberRepository.save(any(GroupMember.class))).thenAnswer(i -> i.getArgument(0));
        groupService.respondToAddingRequest(groupId, groupMemberRequest);
        verify(groupMemberRepository, times(1)).save(any(GroupMember.class));
        verify(groupJoinRequestRepository, times(1)).delete(groupJoinRequest);
    }

    @Test
    void respondToAddingRequest_shouldRejectUser() {
        groupMemberRequest.setStatus(GroupJoinStatus.REJECTED.name());
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(guestId)).thenReturn(Optional.of(guest));
        when(groupJoinRequestRepository.findByGroupIdAndUserId(groupId, guestId)).thenReturn(Optional.of(groupJoinRequest));
        groupService.respondToAddingRequest(groupId, groupMemberRequest);
        verify(groupJoinRequestRepository,times(1)).delete(groupJoinRequest);
    }

    @Test
    void create_shouldThrowIfOwnerNotFound() {
        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> groupService.create(ownerId, createRequest));
    }

    @Test
    void update_shouldThrowIfGroupNotFound() {
        when(groupRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> groupService.update(999L, updateRequest, 1L));
    }

    @Test
    void getGroupMembers_shouldReturnList() {
        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUser(guest);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupId(groupId)).thenReturn(Optional.of(List.of(member)));

        List<UserShortDto> result = groupService.getGroupMembers(groupId);

        assertEquals(1, result.size());
        assertEquals(guest.getId(), result.get(0).getId());
        assertEquals(guest.getFirstName(), result.get(0).getFirstName());
        verify(groupRepository).findById(groupId);
        verify(groupMemberRepository).findByGroupId(groupId);
    }

    @Test
    void getGroupMembers_shouldThrowIfGroupNotFound() {
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> groupService.getGroupMembers(groupId));
    }

    @Test
    void getGroupMembers_shouldReturnEmptyListIfNoMembersFound() {
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupId(groupId)).thenReturn(Optional.empty());

        List<UserShortDto> result = groupService.getGroupMembers(groupId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getAll_shouldReturnPagedGroupsWithMembershipInfo() {
        Group group2 = new Group();
        group2.setId(101L);
        group2.setName("Another Group");
        group2.setPrivate(false);

        Page<Group> groupPage = new PageImpl<>(List.of(group, group2));
        when(groupRepository.findAll(any(Pageable.class))).thenReturn(groupPage);

        when(groupMemberRepository.findByGroupIdAndUserId(group.getId(), guestId)).thenReturn(Optional.of(new GroupMember()));
        when(groupMemberRepository.findByGroupIdAndUserId(group2.getId(), guestId)).thenReturn(Optional.empty());

        Page<GroupResponse> result = groupService.getAll(0, 10, guestId);

        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().get(0).isMember());
        assertFalse(result.getContent().get(1).isMember());
        verify(groupRepository).findAll(any(Pageable.class));
    }
}

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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
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
    private GroupDeleteRequest deleteRequest;
    private GroupAddRequest addRequest;
    private GroupRespondRequest respondRequest;
    private GroupJoinRequest groupJoinRequest;
    private User owner;
    private User guest;
    private Group group;

    @BeforeEach
    void setUp() {
        MockMvcBuilders.standaloneSetup(groupService).build();
        realModelMapper = new ModelMapper();
        realModelMapper.typeMap(GroupCreateRequest.class, Group.class)
                .addMappings(mapper -> mapper.map(GroupCreateRequest::getIsPrivate, Group::setPrivate));
        ReflectionTestUtils.setField(groupService, "modelMapper", realModelMapper);
        owner = new User();
        owner.setId(1L);
        owner.setFirstName("Name");
        owner.setLastName("LastName");
        owner.setEmail("email@email.com");
        owner.setPassword("Password123");

        guest = new User();
        guest.setId(2L);
        guest.setFirstName("Guest");
        guest.setLastName("LastName");
        guest.setEmail("guest@gmail.com");
        guest.setPassword("Password123");

        group = new Group();
        group.setId(100L);
        group.setName("Test Group");
        group.setOwner(owner);
        group.setPrivate(true);

        createRequest = new GroupCreateRequest();
        createRequest.setOwnerId(1L);
        createRequest.setName("New Test Group");
        createRequest.setIsPrivate(true);

        updateRequest = new GroupUpdateRequest();
        updateRequest.setDescription("Updated");
        updateRequest.setColor("#000000");
        updateRequest.setImgUrl("http://test.png");

        deleteRequest = new GroupDeleteRequest();
        deleteRequest.setOwnerId(1L);

        addRequest = new GroupAddRequest();
        addRequest.setGroupId(100L);
        addRequest.setUserId(2L);
        addRequest.setInitiatedBy(2L);

        respondRequest = new GroupRespondRequest();
        respondRequest.setGroupId(100L);
        respondRequest.setUserId(2L);
        respondRequest.setStatus(GroupJoinStatus.APPROVED);

        groupJoinRequest = new GroupJoinRequest();
        groupJoinRequest.setGroup(group);
        groupJoinRequest.setUser(guest);
        groupJoinRequest.setStatus(GroupJoinStatus.PENDING);
    }

    @Test
    void create_shouldSaveGroup() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(groupRepository.save(any(Group.class))).thenAnswer(i -> i.getArgument(0));
        GroupResponse response = groupService.create(createRequest);
        assertEquals(createRequest.getName(), response.getName());
        assertTrue(response.isPrivate());
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    @Test
    void update_shouldUpdateGroupFields() {
        when(groupRepository.findById(100L)).thenReturn(Optional.of(group));
        when(groupRepository.save(any(Group.class))).thenAnswer(i -> i.getArgument(0));
        groupService.update(100L, updateRequest);
        assertEquals("Updated", group.getDescription());
        assertEquals("#000000", group.getColor());
        assertEquals("http://test.png", group.getImageUrl());
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    @Test
    void delete_shouldDeleteGroup() {
        when(groupRepository.findById(100L)).thenReturn(Optional.of(group));
        groupService.delete(100L, deleteRequest);
        verify(groupRepository).deleteById(100L);
    }

    @Test
    void addUserToGroup_shouldHandlePrivateGroup() {
        when(groupRepository.findById(100L)).thenReturn(Optional.of(group));
        when(userRepository.findById(2L)).thenReturn(Optional.of(guest));
        when(groupJoinRequestRepository.findTop1ByGroup_IdAndUser_IdOrderByCreatedDateDesc(100L, 2L))
                .thenReturn(Optional.empty());
        groupService.addUserToGroup(addRequest);
        verify(groupJoinRequestRepository, times(1)).save(any(GroupJoinRequest.class));
    }


    @Test
    void respondToAddingRequest_shouldApproveUser() {
        respondRequest.setStatus(GroupJoinStatus.APPROVED);
        when(groupRepository.findById(100L)).thenReturn(Optional.of(group));
        when(userRepository.findById(2L)).thenReturn(Optional.of(guest));
        when(groupJoinRequestRepository.findByGroupIdAndUserId(100L, 2L)).thenReturn(Optional.of(groupJoinRequest));
        groupService.respondToAddingRequest(respondRequest);
        verify(groupMemberRepository, times(1)).save(any(GroupMember.class));
        verify(groupJoinRequestRepository, times(1)).delete(groupJoinRequest);
    }

    @Test
    void respondToAddingRequest_shouldRejectUser() {
        respondRequest.setStatus(GroupJoinStatus.REJECTED);
        when(groupRepository.findById(100L)).thenReturn(Optional.of(group));
        when(userRepository.findById(2L)).thenReturn(Optional.of(guest));
        when(groupJoinRequestRepository.findByGroupIdAndUserId(100L, 2L)).thenReturn(Optional.of(groupJoinRequest));
        groupService.respondToAddingRequest(respondRequest);
        verify(groupJoinRequestRepository,times(1)).delete(groupJoinRequest);
    }

    @Test
    void create_shouldThrowIfOwnerNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> groupService.create(createRequest));
    }

    @Test
    void update_shouldThrowIfGroupNotFound() {
        when(groupRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> groupService.update(999L, updateRequest));
    }
}

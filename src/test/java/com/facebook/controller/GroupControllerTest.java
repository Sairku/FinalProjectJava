package com.facebook.controller;

import com.facebook.dto.*;
import com.facebook.enums.GroupJoinStatus;
import com.facebook.enums.Provider;
import com.facebook.middleware.CurrentUserArgumentResolver;
import com.facebook.model.Group;
import com.facebook.model.User;
import com.facebook.service.GroupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class GroupControllerTest {
    @Mock
    private GroupService groupService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private GroupController groupController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private UserAuthDto currentUserData;
    private GroupCreateRequest createRequest;
    private GroupUpdateRequest updateRequest;
    private GroupMemberRequest groupMemberRequest;
    private GroupResponse groupResponse;
    private Long userId = 1L;
    private Long groupId = 100L;

    private MockMvc buildMockMvc(boolean withCurrentUser) {
        StandaloneMockMvcBuilder builder = MockMvcBuilders.standaloneSetup(groupController);

        if (withCurrentUser) {
            builder.setCustomArgumentResolvers(new CurrentUserArgumentResolver());

            UserAuthDto currentUserData = new UserAuthDto(userId, "test@example.com", "test", Provider.LOCAL, new ArrayList<>());

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(currentUserData);

            SecurityContextHolder.setContext(securityContext);
        }
        return builder.build();
    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        createRequest = new GroupCreateRequest();
        createRequest.setName("Test Group");
        createRequest.setIsPrivate(true);

        updateRequest = new GroupUpdateRequest();
        updateRequest.setDescription("Some desc");
        updateRequest.setColor("#FFFFFF");

        groupMemberRequest = new GroupMemberRequest();
        groupMemberRequest.setUserId(userId);
        groupMemberRequest.setStatus(GroupJoinStatus.APPROVED.name());

        groupResponse = new GroupResponse();
        groupResponse.setId(groupId);
        groupResponse.setName(createRequest.getName());
        groupResponse.setDescription(updateRequest.getDescription());
        groupResponse.setColor(updateRequest.getColor());
        groupResponse.setPrivate(createRequest.getIsPrivate());
    }

    @Test
    void createGroup_shouldReturn201() throws Exception {
        mockMvc = buildMockMvc(true);

        Mockito.when(groupService.create(eq(userId), any(GroupCreateRequest.class))).thenReturn(groupResponse);

        mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Group was created"))
                .andExpect(jsonPath("$.data.name").value("Test Group"));
    }

    @Test
    void updateGroup_shouldReturn200() throws Exception {
        mockMvc = buildMockMvc(true);

        Mockito.when(groupService.update(eq(groupId), any(GroupUpdateRequest.class), eq(userId)))
                .thenReturn(groupResponse);

        mockMvc.perform(put("/api/groups/" + groupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Group was updated"));
    }

    @Test
    void deleteGroup_shouldReturn200() throws Exception {
        mockMvc = buildMockMvc(true);

        Mockito.doNothing().when(groupService).delete(eq(groupId), eq(userId));

        mockMvc.perform(delete("/api/groups/" + groupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Group was deleted"));
    }

    @Test
    void addUserToGroup_privateGroup_shouldReturnRequestMessage() throws Exception {
        mockMvc = buildMockMvc(true);
        Group group = new Group(
                groupResponse.getName(),
                groupResponse.getDescription(),
                groupResponse.getImageUrl(),
                groupResponse.getColor(),
                true,
                new User()
        );

        Mockito.doNothing().when(groupService).addUserToGroup(groupId, userId, userId);

        mockMvc.perform(post("/api/groups/" + groupId + "/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupMemberRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Request for adding to the group")));
    }

    @Test
    void addUserToGroup_publicGroup_shouldReturnAddedMessage() throws Exception {
        mockMvc = buildMockMvc(true);
        Group group = new Group(
                groupResponse.getName(),
                groupResponse.getDescription(),
                groupResponse.getImageUrl(),
                groupResponse.getColor(),
                false,
                new User()
        );

        Mockito.doNothing().when(groupService).addUserToGroup(groupId, userId, userId);

        Mockito.when(groupService.findById(groupId)).thenReturn(group);

        mockMvc.perform(post("/api/groups/" + groupId + "/join"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("was added to the group")));
    }

    @Test
    void respondToGroup_approved_shouldReturnAddedMessage() throws Exception {
        mockMvc = buildMockMvc(false);

        Mockito.doNothing().when(groupService).respondToAddingRequest(eq(groupId), any(GroupMemberRequest.class));

        mockMvc.perform(put("/api/groups/" + groupId + "/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupMemberRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("was added to the group")));
    }

    @Test
    void respondToGroup_rejected_shouldReturnRejectedMessage() throws Exception {
        mockMvc = buildMockMvc(false);

        groupMemberRequest.setStatus(GroupJoinStatus.REJECTED.name());
        Mockito.doNothing().when(groupService).respondToAddingRequest(eq(groupId), any(GroupMemberRequest.class));

        mockMvc.perform(put("/api/groups/" + groupId + "/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupMemberRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("was rejected to join to the group")));
    }

    @Test
    void getAllGroups_shouldReturnPagedGroups() throws Exception {
        mockMvc = buildMockMvc(true);

        GroupResponse group1 = new GroupResponse();
        group1.setId(1L);
        group1.setName("Group 1");
        group1.setMember(true);

        GroupResponse group2 = new GroupResponse();
        group2.setId(2L);
        group2.setName("Group 2");
        group2.setMember(false);

        Page<GroupResponse> groupPage = new PageImpl<>(new ArrayList<>(List.of(group1, group2)));

        when(groupService.getAll(0, 10, userId)).thenReturn(groupPage);

        mockMvc.perform(get("/api/groups?page=0&size=10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Groups retrieved successfully"))
                .andExpect(jsonPath("$.data.content[0].name").value("Group 1"))
                .andExpect(jsonPath("$.data.content[1].name").value("Group 2"));
    }

    @Test
    void getGroupMembers_shouldReturnListOfMembers() throws Exception {
        mockMvc = buildMockMvc(false);

        UserShortDto member1 = new UserShortDto();
        member1.setId(10L);
        member1.setFirstName("Alice");

        UserShortDto member2 = new UserShortDto();
        member2.setId(11L);
        member2.setFirstName("Bob");

        when(groupService.getGroupMembers(groupId)).thenReturn(List.of(member1, member2));

        mockMvc.perform(get("/api/groups/" + groupId + "/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Group members retrieved successfully"))
                .andExpect(jsonPath("$.data[0].firstName").value("Alice"))
                .andExpect(jsonPath("$.data[1].firstName").value("Bob"));
    }
}

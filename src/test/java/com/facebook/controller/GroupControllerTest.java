package com.facebook.controller;

import com.facebook.dto.*;
import com.facebook.enums.GroupJoinStatus;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class GroupControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GroupService groupService;

    @InjectMocks
    private GroupController groupController;

    private ObjectMapper objectMapper;

    private GroupCreateRequest createRequest;
    private GroupUpdateRequest updateRequest;
    private GroupDeleteRequest deleteRequest;
    private GroupAddRequest addRequest;
    private GroupRespondRequest respondRequest;
    private GroupResponse groupResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(groupController).build();

        createRequest = new GroupCreateRequest();
        createRequest.setOwnerId(1L);
        createRequest.setName("Test Group");
        createRequest.setIsPrivate(true);

        updateRequest = new GroupUpdateRequest();
        updateRequest.setDescription("Some desc");
        updateRequest.setColor("#FFFFFF");
        updateRequest.setImgUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/b/bd/Test.svg/1200px-Test.svg.png");

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

        groupResponse = new GroupResponse();
        groupResponse.setName(createRequest.getName());
        groupResponse.setDescription(updateRequest.getDescription());
        groupResponse.setColor(updateRequest.getColor());
        groupResponse.setPrivate(createRequest.getIsPrivate());
    }

    @Test
    void createGroup_shouldReturn201() throws Exception {
        Mockito.when(groupService.create(any(GroupCreateRequest.class))).thenReturn(groupResponse);

        mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Group was created"))
                .andExpect(jsonPath("$.data.name").value("Test Group"));
    }

    @Test
    void updateGroup_shouldReturn200() throws Exception {
        Mockito.when(groupService.update(eq(100L), any(GroupUpdateRequest.class)))
                .thenReturn(groupResponse);

        mockMvc.perform(put("/api/groups/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Group was updated"));
    }

    @Test
    void deleteGroup_shouldReturn200() throws Exception {
        Mockito.doNothing().when(groupService).delete(eq(100L), any(GroupDeleteRequest.class));

        mockMvc.perform(delete("/api/groups/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Group was deleted"));
    }

    @Test
    void addUserToGroup_privateGroup_shouldReturnRequestMessage() throws Exception {
        Mockito.doNothing().when(groupService).addUserToGroup(any(GroupAddRequest.class));
        Mockito.when(groupService.findById(100L)).thenReturn(new Group(
                groupResponse.getName(), groupResponse.getDescription(),
                groupResponse.getImageUrl(),groupResponse.getColor(),
                groupResponse.isPrivate(),new User()));

        mockMvc.perform(post("/api/groups/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Request for adding to the group")));
    }

    @Test
    void addUserToGroup_publicGroup_shouldReturnAddedMessage() throws Exception {
        Mockito.doNothing().when(groupService).addUserToGroup(any(GroupAddRequest.class));
        Mockito.when(groupService.findById(100L)).thenReturn(new Group(
                groupResponse.getName(), groupResponse.getDescription(),
                groupResponse.getImageUrl(),groupResponse.getColor(),
                !groupResponse.isPrivate(),new User()));

        mockMvc.perform(post("/api/groups/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("was added to the group")));
    }

    @Test
    void respondToGroup_approved_shouldReturnAddedMessage() throws Exception {
        Mockito.doNothing().when(groupService).respondToAddingRequest(any(GroupRespondRequest.class));

        mockMvc.perform(put("/api/groups/respond")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(respondRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("was added to the group")));
    }

    @Test
    void respondToGroup_rejected_shouldReturnRejectedMessage() throws Exception {
        respondRequest.setStatus(GroupJoinStatus.REJECTED);
        Mockito.doNothing().when(groupService).respondToAddingRequest(any(GroupRespondRequest.class));

        mockMvc.perform(put("/api/groups/respond")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(respondRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("was rejected to join to the group")));
    }
}

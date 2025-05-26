package com.facebook.controller;

import com.facebook.dto.FriendRequest;
import com.facebook.enums.FriendStatus;
import com.facebook.service.FriendService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FriendControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FriendService friendService;

    @InjectMocks
    private FriendController friendController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(friendController).build();
    }

    @Test
    void addFriend_shouldReturn200_whenValid() throws Exception {
        FriendRequest request = new FriendRequest();
        request.setUserId(1L);
        request.setFriendId(2L);

        Mockito.when(friendService.addFriendRequest(1L, 2L))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body("Request sent"));

        mockMvc.perform(post("/api/friends/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(friendService).addFriendRequest(1L, 2L);
    }

    @Test
    void addFriend_shouldReturn400_whenSendingRequestToYourself() throws Exception {
        FriendRequest request = new FriendRequest();
        request.setUserId(1L);
        request.setFriendId(1L); // same ID

        mockMvc.perform(post("/api/friends/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You cannot send a friend request to yourself"));
    }

    @Test
    void respondToFriendRequest_shouldReturn200() throws Exception {
        FriendRequest request = new FriendRequest();
        request.setUserId(1L);
        request.setFriendId(2L);
        request.setStatus(FriendStatus.ACCEPTED);

        Mockito.when(friendService.responseToFriendRequest(1L, 2L, FriendStatus.ACCEPTED))
                .thenReturn(ResponseEntity.ok("Accepted"));

        mockMvc.perform(put("/api/friends/respond")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(friendService).responseToFriendRequest(1L, 2L, FriendStatus.ACCEPTED);
    }

    @Test
    void deleteFriend_shouldReturn200() throws Exception {
        FriendRequest request = new FriendRequest();
        request.setUserId(1L);
        request.setFriendId(2L);

        Mockito.when(friendService.deleteFriend(1L, 2L))
                .thenReturn(ResponseEntity.ok("Deleted"));

        mockMvc.perform(delete("/api/friends/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(friendService).deleteFriend(1L, 2L);
    }
}

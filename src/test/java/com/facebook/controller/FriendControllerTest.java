package com.facebook.controller;

import com.facebook.annotation.CurrentUser;
import com.facebook.dto.FriendRequest;
import com.facebook.dto.UserAuthDto;
import com.facebook.enums.FriendStatus;
import com.facebook.enums.Provider;
import com.facebook.middleware.CurrentUserArgumentResolver;
import com.facebook.model.User;
import com.facebook.service.FriendService;
import com.facebook.util.ResponseHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class FriendControllerTest {
    @Mock
    private FriendService friendService;

    @Mock
    private ResponseHandler responseHandler;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private FriendController friendController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UserAuthDto currentUser;
    private User testFriend;
    private Long userId = 1L;
    private Long friendId = 2L;

    private MockMvc buildMockMvc(boolean withCurrentUser) {
        StandaloneMockMvcBuilder builder = MockMvcBuilders.standaloneSetup(friendController);

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

        testFriend = new User();
        testFriend.setId(friendId);
        testFriend.setFirstName("Test Friend");

        mockMvc = buildMockMvc(true);
    }

    @Test
    void addFriend_shouldReturnSuccess() throws Exception {
        when(friendService.addFriendRequest(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok("Friend request sent"));

        mockMvc.perform(get("/api/friends/add/" + friendId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Friend request sent")));

    }

    @Test
    void addFriend_selfRequest_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/friends/add/" + userId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.message").value("You cannot send a friend request to yourself"));

    }

    @Test
    void respondToFriendRequest_accept_shouldReturnSuccess() throws Exception {
        when(friendService.responseToFriendRequest(anyLong(), anyLong(), eq(FriendStatus.ACCEPTED)))
                .thenReturn(ResponseEntity.ok("Friend request accepted"));

        mockMvc.perform(get("/api/friends/respond/" + friendId + "/accept"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Friend request accepted")));

    }

    @Test
    void respondToFriendRequest_decline_shouldReturnSuccess() throws Exception {
        when(friendService.responseToFriendRequest(anyLong(), anyLong(), eq(FriendStatus.DECLINED)))
                .thenReturn(ResponseEntity.ok("Friend request declined"));

        mockMvc.perform(get("/api/friends/respond/" + friendId + "/decline"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Friend request declined")));

    }

    @Test
    void respondToFriendRequest_invalidStatus_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/friends/respond/" + friendId + "/invalid"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.message").value("Invalid status"));

    }

    @Test
    void deleteFriend_shouldReturnSuccess() throws Exception {
        when(friendService.deleteFriend(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok("Friend removed"));

        mockMvc.perform(delete("/api/friends/delete/" + friendId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Friend removed")));

    }

    @Test
    void getFriends_shouldReturnFriendList() throws Exception {
        List<User> friends = Arrays.asList(testFriend);
        when(friendService.getAllFriendUsers(anyLong())).thenReturn(friends);

        mockMvc.perform(get("/api/friends/get-friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(friendId))
                .andExpect(jsonPath("$[0].firstName").value("Test Friend"));

    }

    @Test
    void getRequests_shouldReturnRequestList() throws Exception {
        List<User> requests = Arrays.asList(testFriend);
        when(friendService.getAllUsersWhoHaveNotYetAccepted(anyLong())).thenReturn(requests);

        mockMvc.perform(get("/api/friends/get-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(friendId))
                .andExpect(jsonPath("$[0].firstName").value("Test Friend"));

    }

    @Test
    void getRecommendedFriends_shouldReturnRecommendedList() throws Exception {
        List<User> recommended = Arrays.asList(testFriend);
        when(friendService.getRecommendedFriends(anyLong())).thenReturn(recommended);

        mockMvc.perform(get("/api/friends/recommended"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(friendId))
                .andExpect(jsonPath("$[0].firstName").value("Test Friend"));

    }
}
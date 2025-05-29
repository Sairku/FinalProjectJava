package com.facebook.controller;

import com.facebook.dto.UserAuthDto;
import com.facebook.dto.UserDetailsDto;
import com.facebook.dto.UserUpdateRequestDto;
import com.facebook.enums.Provider;
import com.facebook.middleware.CurrentUserArgumentResolver;
import com.facebook.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @Mock
    private UserService userService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private long userId = 1L;
    private UserAuthDto currentUserData;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setCustomArgumentResolvers(new CurrentUserArgumentResolver())
                .build();
        currentUserData = new UserAuthDto(userId, "test@example.com", "test", Provider.LOCAL, new ArrayList<>());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(currentUserData);
    }

    @Test
    void testGetUserDetailsCurrentUser() throws Exception {
        UserDetailsDto mockDetails = new UserDetailsDto();
        mockDetails.setId(userId);
        mockDetails.setEmail("test@example.com");
        mockDetails.setFirstName("First Name");
        mockDetails.setLastName("Last Name");

        when(userService.getCurrentUserDetails(userId)).thenReturn(mockDetails);

        mockMvc.perform(get("/api/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User details retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("First Name"))
                .andExpect(jsonPath("$.data.lastName").value("Last Name"));

        verify(userService, times(1)).getCurrentUserDetails(userId);
    }

    @Test
    void testGetUserDetailsOtherUser() throws Exception {
        long otherUserId = 2L;

        UserDetailsDto friendDetails = new UserDetailsDto();
        friendDetails.setId(otherUserId);
        friendDetails.setEmail("friend@example.com");
        friendDetails.setFirstName("Friend First Name");

        when(userService.getUserDetails(otherUserId, userId)).thenReturn(friendDetails);

        mockMvc.perform(get("/api/users/{userId}", otherUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User details retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(otherUserId))
                .andExpect(jsonPath("$.data.email").value("friend@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("Friend First Name"));

        verify(userService, times(1)).getUserDetails(otherUserId, userId);
    }

    @Test
    void testUpdateUserSuccess() throws Exception {
        UserUpdateRequestDto updateDto = new UserUpdateRequestDto();
        updateDto.setFirstName("Updated First Name");
        updateDto.setLastName("Updated Last Name");

        UserDetailsDto updatedDetails = new UserDetailsDto();
        updatedDetails.setId(userId);
        updatedDetails.setFirstName("Updated First Name");
        updatedDetails.setLastName("Updated Last Name");

        when(userService.updateUser(eq(userId), any(UserUpdateRequestDto.class))).thenReturn(updatedDetails);

        mockMvc.perform(put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User updated successfully"))
                .andExpect(jsonPath("$.data.firstName").value("Updated First Name"))
                .andExpect(jsonPath("$.data.lastName").value("Updated Last Name"));

        verify(userService, times(1)).updateUser(eq(userId), any(UserUpdateRequestDto.class));
    }
}

package com.facebook.controller;

import com.facebook.dto.UserAuthDto;
import com.facebook.dto.UserDetailsDto;
import com.facebook.dto.UserShortDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

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

        mockMvc.perform(get("/api/users/current"))
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

    @Test
    void testGetAllUsersExceptCurrent() throws Exception {
        UserShortDto user1 = new UserShortDto(2L, "Alice", "Smith", null, null);
        UserShortDto user2 = new UserShortDto(3L, "Bob", "Jones", null, null);

        Page<UserShortDto> mockPage = new PageImpl<>(
                List.of(user1, user2),
                PageRequest.of(0, 10),
                2
        );

        when(userService.getAllUsersExceptCurrent(userId, 0, 10)).thenReturn(mockPage);

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Users retrieved successfully"))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].id").value(2L))
                .andExpect(jsonPath("$.data.content[0].firstName").value("Alice"))
                .andExpect(jsonPath("$.data.content[1].id").value(3L))
                .andExpect(jsonPath("$.data.content[1].firstName").value("Bob"));

        verify(userService, times(1)).getAllUsersExceptCurrent(userId, 0, 10);
    }

    @Test
    void searchUsersByFullName_returnsUsersAndOkMessage() throws Exception {
        String query = "John Doe";

        UserShortDto dto = new UserShortDto();
        dto.setId(1L);
        dto.setFirstName("John");
        dto.setLastName("Doe");

        List<UserShortDto> resultList = List.of(dto);

        when(userService.searchUsersByFullName(userId, query)).thenReturn(resultList);

        mockMvc.perform(get("/api/users/search")
                        .param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.message").value("The search by \"John Doe\" yielded results"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].firstName").value("John"))
                .andExpect(jsonPath("$.data[0].lastName").value("Doe"));

        verify(userService, times(1)).searchUsersByFullName(userId, query);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(currentUserData);
    }

    @Test
    void searchUsersByFullName_returnsEmptyListAndUnsuccessfulMessage() throws Exception {
        String query = "Wrong Name";

        List<UserShortDto> emptyList = List.of();

        when(userService.searchUsersByFullName(userId, query)).thenReturn(emptyList);

        mockMvc.perform(get("/api/users/search")
                        .param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.message").value("No users found for \"Wrong Name\""))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(userService, times(1)).searchUsersByFullName(userId, query);

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(currentUserData);
    }
}

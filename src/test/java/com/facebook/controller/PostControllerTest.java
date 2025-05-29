package com.facebook.controller;

import com.facebook.dto.*;
import com.facebook.enums.Provider;
import com.facebook.middleware.CurrentUserArgumentResolver;
import com.facebook.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class PostControllerTest {
    @Mock
    private PostService postService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PostController postController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Long userId = 1L;

    private MockMvc buildMockMvc(boolean withCurrentUser) {
        StandaloneMockMvcBuilder builder = MockMvcBuilders.standaloneSetup(postController);

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
    }

    @Test
    void createPost_shouldReturn201() throws Exception {
        mockMvc = buildMockMvc(true);

        PostCreateRequest request = new PostCreateRequest();
        request.setDescription("Test post");
        request.setImages(List.of("http://image.com/test.jpg"));

        PostResponse response = new PostResponse();
        response.setDescription(request.getDescription());
        response.setImages(request.getImages());
        response.setUser(new UserShortDto(userId, "John", "Doe"));

        // замість @CurrentUser — передаємо явно
        Mockito.when(postService.createPost(eq(userId), any(PostCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/posts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Post was created"))
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.data.description").value("Test post"));
    }

    @Test
    void updatePost_shouldReturn200() throws Exception {
        mockMvc = buildMockMvc(false);

        Long postId = 1L;

        PostUpdateRequest request = new PostUpdateRequest();
        request.setDescription("Updated description");
        request.setImages(List.of("http://image.com/updated.jpg"));

        PostResponse response = new PostResponse();
        response.setDescription(request.getDescription());
        response.setImages(request.getImages());

        Mockito.when(postService.updatePost(postId, request)).thenReturn(response);

        mockMvc.perform(put("/api/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Post was updated"))
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.data.description").value("Updated description"));
    }

    @Test
    void createPost_shouldReturn400_whenInvalidRequest() throws Exception {
        mockMvc = buildMockMvc(false);

        PostCreateRequest invalidRequest = new PostCreateRequest(); // порожній

        mockMvc.perform(post("/api/posts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
    @Test
    void deletePost_shouldReturn200_whenDeleted() throws Exception {
        Long postId = 1L;

        mockMvc = buildMockMvc(false);
        Mockito.doNothing().when(postService).deletePost(postId);

        mockMvc.perform(delete("/api/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Post was deleted"))
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}

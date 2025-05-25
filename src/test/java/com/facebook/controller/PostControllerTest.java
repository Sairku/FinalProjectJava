package com.facebook.controller;

import com.facebook.dto.PostCreateRequest;
import com.facebook.dto.PostResponse;
import com.facebook.dto.PostUpdateRequest;
import com.facebook.dto.UserShortDto;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class PostControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PostService postService;

    private ObjectMapper objectMapper;

    @InjectMocks
    private PostController postController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(postController).build();
    }

    @Test
    void createPost_shouldReturn201() throws Exception {
        PostCreateRequest request = new PostCreateRequest();
        request.setDescription("Test post");
        request.setImages(List.of("http://image.com/test.jpg"));

        Long mockUserId = 1L;

        PostResponse response = new PostResponse();
        response.setDescription(request.getDescription());
        response.setImages(request.getImages());
        response.setUser(new UserShortDto(mockUserId, "John", "Doe"));

        // замість @CurrentUser — передаємо явно
        Mockito.when(postService.createPost(eq(mockUserId), any(PostCreateRequest.class))).thenReturn(response);

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
        PostCreateRequest invalidRequest = new PostCreateRequest(); // порожній

        mockMvc.perform(post("/api/posts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}

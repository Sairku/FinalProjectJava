package com.facebook.controller;

import com.facebook.dto.PostCreateRequest;
import com.facebook.dto.PostResponse;
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
        request.setUserId(1L);
        request.setDescription("Test post");
        request.setImgUrl("http://image.com/test.jpg");

        PostResponse response = new PostResponse();
        response.setDescription(request.getDescription());
        response.setImgUrl(request.getImgUrl());

        Mockito.when(postService.createPost(request)).thenReturn(response);

        mockMvc.perform(post("/api/posts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Post was created"))
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.data.description").value("Test post"));
    }
}


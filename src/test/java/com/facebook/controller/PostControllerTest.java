package com.facebook.controller;

import com.facebook.dto.*;
import com.facebook.enums.Provider;
import com.facebook.middleware.CurrentUserArgumentResolver;
import com.facebook.service.CommentService;
import com.facebook.service.PostService;
import com.facebook.service.UserAchievementService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.time.LocalDateTime;
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
    private CommentService commentService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private UserAchievementService userAchievementService;

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

        PostCreateRequestDto request = new PostCreateRequestDto();
        request.setText("Test post");
        request.setImages(List.of("http://image.com/test.jpg"));

        PostResponseDto response = new PostResponseDto();
        response.setText(request.getText());
        response.setImages(request.getImages());
        response.setUser(new UserShortDto(userId, "John", "Doe", null, null));

        // замість @CurrentUser — передаємо явно
        Mockito.when(postService.createPost(eq(userId), any(PostCreateRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/posts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Post was created"))
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.data.text").value("Test post"));
    }

    @Test
    void updatePost_shouldReturn200() throws Exception {
        mockMvc = buildMockMvc(false);

        Long postId = 1L;

        PostUpdateRequestDto request = new PostUpdateRequestDto();
        request.setText("Updated description");
        request.setImages(List.of("http://image.com/updated.jpg"));

        PostResponseDto response = new PostResponseDto();
        response.setText(request.getText());
        response.setImages(request.getImages());

        Mockito.when(postService.updatePost(postId, request)).thenReturn(response);

        mockMvc.perform(put("/api/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Post was updated"))
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.data.text").value("Updated description"));
    }

    @Test
    void createPost_shouldReturn400_whenInvalidRequest() throws Exception {
        mockMvc = buildMockMvc(false);

        PostCreateRequestDto invalidRequest = new PostCreateRequestDto(); // порожній

        mockMvc.perform(post("/api/posts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletePost_shouldReturn200_whenDeleted() throws Exception {
        Long postId = 1L;

        mockMvc = buildMockMvc(true);
        Mockito.doNothing().when(postService).deletePost(postId, userId);

        mockMvc.perform(delete("/api/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Post was deleted"))
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void likePost_shouldReturn200WithLikesCount() throws Exception {
        mockMvc = buildMockMvc(true);
        Long postId = 1L;
        int likesCount = 5;

        when(postService.likePost(postId, userId)).thenReturn(likesCount);

        mockMvc.perform(post("/api/posts/{id}/like", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Post liked successfully"))
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.data").value(5));
    }

    @Test
    void repostPost_Success() throws Exception {
        mockMvc = buildMockMvc(true);
        Long postId = 1L;

        when(postService.repost(postId, userId)).thenReturn(1);

        mockMvc.perform(post("/api/posts/{id}/repost", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Post reposted successfully"))
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void commentPost_shouldReturn201WithCommentData() throws Exception {
        mockMvc = buildMockMvc(true);
        Long postId = 1L;
        CommentRequestDto request = new CommentRequestDto();
        request.setText("Great post!");

        CommentResponseDto commentResponse = new CommentResponseDto();
        commentResponse.setId(1L);
        commentResponse.setText("Great post!");
        commentResponse.setUser(new UserShortDto(userId, "John", "Doe", null, null));

        when(commentService.addComment(postId, userId, "Great post!")).thenReturn(commentResponse);

        mockMvc.perform(post("/api/posts/{id}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Comment added successfully"))
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.data.text").value("Great post!"));
    }

    @Test
    void getPostComments_shouldReturn200WithCommentList() throws Exception {
        mockMvc = buildMockMvc(false);
        Long postId = 1L;

        List<CommentResponseDto> comments = List.of(
                new CommentResponseDto(1L, new UserShortDto(userId, "John", "Doe", null, null), "Nice!", null)
        );

        when(commentService.getPostComments(postId)).thenReturn(comments);

        mockMvc.perform(get("/api/posts/{id}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comments retrieved successfully"))
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.data[0].text").value("Nice!"));
    }

    @Test
    void testGetAllPosts() throws Exception {
        mockMvc = buildMockMvc(true);

        List<PostResponseDto> posts = new ArrayList<>();
        PostResponseDto post = new PostResponseDto();

        post.setId(1L);
        post.setText("Test post");
        post.setCreatedDate(LocalDateTime.now());
        post.setUser(new UserShortDto(userId, "John", "Doe", null, null));
        posts.add(post);

        when(postService.getUserAndFriendsPosts(userId)).thenReturn(posts);

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Posts retrieved successfully"))
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.data[0].text").value("Test post"));
    }
}

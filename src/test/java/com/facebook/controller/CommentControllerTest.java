package com.facebook.controller;

import com.facebook.dto.CommentRequestDto;
import com.facebook.dto.UserAuthDto;
import com.facebook.enums.Provider;
import com.facebook.middleware.CurrentUserArgumentResolver;
import com.facebook.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.util.ArrayList;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class CommentControllerTest {
    @Mock
    private CommentService commentService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CommentController commentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Long userId = 1L;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        StandaloneMockMvcBuilder builder = MockMvcBuilders.standaloneSetup(commentController);

        builder.setCustomArgumentResolvers(new CurrentUserArgumentResolver());

        UserAuthDto currentUserData = new UserAuthDto(userId, "test@example.com", "test", Provider.LOCAL, new ArrayList<>());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(currentUserData);

        SecurityContextHolder.setContext(securityContext);

        mockMvc = builder.build();
    }

    @Test
    void updateCommentSuccess() throws Exception {
        Long commentId = 10L;
        String newText = "Updated comment content";

        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setText(newText);

        Mockito.doNothing().when(commentService).updateComment(commentId, userId, requestDto.getText());

        mockMvc.perform(put("/api/comments/{commentId}", commentId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comment updated successfully"))
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void deleteCommentSuccess() throws Exception {
        Long commentId = 10L;

        Mockito.doNothing().when(commentService).deleteComment(commentId, userId);

        mockMvc.perform(delete("/api/comments/{commentId}", commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comment deleted successfully"))
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}

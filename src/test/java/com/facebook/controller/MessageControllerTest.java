package com.facebook.controller;

import com.facebook.enums.Provider;
import com.facebook.middleware.CurrentUserArgumentResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.facebook.dto.*;
import com.facebook.service.MessageService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class MessageControllerTest {

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private MessageController messageController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private MessageResponse sampleResponse;

    private UserAuthDto currentUserData;

    private MockMvc buildMockMvc(boolean withCurrentUser) {
        StandaloneMockMvcBuilder builder = MockMvcBuilders.standaloneSetup(messageController);

        if (withCurrentUser) {
            builder.setCustomArgumentResolvers(new CurrentUserArgumentResolver());

            UserAuthDto currentUserData = new UserAuthDto(1L, "test@example.com", "test", Provider.LOCAL, new ArrayList<>());

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
    void create_shouldReturn201() throws Exception {
        mockMvc = buildMockMvc(true);
        // Оновлений запит без ID в MessageCreateRequest, оскільки ID отримується з @CurrentUser
        MessageCreateRequest request = new MessageCreateRequest(2L, "Hello!");

        mockMvc.perform(post("/api/messages/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Message created"))
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.text").value("Hello!"));
    }

    @Test
    void edit_shouldReturn200() throws Exception {
        mockMvc = buildMockMvc(true);
        MessageUpdateRequest request = new MessageUpdateRequest("Updated text");

        mockMvc.perform(put("/api/messages/edit/{id}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Message updated"))
                .andExpect(jsonPath("$.error").value(false));
    }

    @Test
    void edit_shouldReturn400_ifIdMismatch() throws Exception {
        mockMvc = buildMockMvc(true);
        MessageUpdateRequest request = new MessageUpdateRequest("Mismatch");

        mockMvc.perform(put("/api/messages/edit/{id}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void markRead_shouldReturn200() throws Exception {
        mockMvc = buildMockMvc(false);
        when(messageService.read(eq(10L))).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/messages/read/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Message marked as read"))
                .andExpect(jsonPath("$.error").value(false));
    }

    @Test
    void delete_shouldReturn200() throws Exception {
        mockMvc = buildMockMvc(true);
        doNothing().when(messageService).delete(10L, 1L);

        mockMvc.perform(delete("/api/messages/delete/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Message deleted"))
                .andExpect(jsonPath("$.error").value(false));
    }
}

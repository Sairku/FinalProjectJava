package com.facebook.controller;

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

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class MessageControllerTest {

    @Mock
    private MessageService messageService;

    @InjectMocks
    private MessageController messageController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private MessageResponse sampleResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(messageController).build();

        UserShortDto sender = new UserShortDto(1L, "John", "Doe");
        UserShortDto receiver = new UserShortDto(2L, "Jane", "Smith");

        sampleResponse = new MessageResponse(10L, sender, receiver, "Hello!", false, LocalDateTime.now());
    }

    @Test
    void create_shouldReturn201() throws Exception {
        MessageCreateRequest request = new MessageCreateRequest(1L, 2L, "Hello!");

        Mockito.when(messageService.create(any(MessageCreateRequest.class))).thenReturn(sampleResponse);

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
        MessageUpdateRequest request = new MessageUpdateRequest(10L, "Updated text");

        Mockito.when(messageService.update(any(MessageUpdateRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/messages/edit/{id}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Message updated"))
                .andExpect(jsonPath("$.error").value(false));
    }

    @Test
    void edit_shouldReturn400_ifIdMismatch() throws Exception {
        MessageUpdateRequest request = new MessageUpdateRequest(11L, "Mismatch");

        mockMvc.perform(put("/api/messages/edit/{id}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void markRead_shouldReturn200() throws Exception {
        Mockito.when(messageService.read(eq(10L))).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/messages/read/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Message marked as read"))
                .andExpect(jsonPath("$.error").value(false));
    }

    @Test
    void delete_shouldReturn200() throws Exception {
        doNothing().when(messageService).delete(10L);

        mockMvc.perform(delete("/api/messages/delete/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Message deleted"))
                .andExpect(jsonPath("$.error").value(false));
    }
}

package com.facebook.service;

import com.facebook.dto.MessageCreateRequest;
import com.facebook.dto.MessageResponse;
import com.facebook.dto.MessageUpdateRequest;
import com.facebook.dto.UserShortDto;
import com.facebook.exception.NotFoundException;
import com.facebook.model.Message;
import com.facebook.model.User;
import com.facebook.repository.MessageRepository;
import com.facebook.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @Mock(lenient = true) // дозволяє не викликати помилку для непотрібних стубів
    private MessageRepository messageRepository;

    @Mock(lenient = true) // дозволяє не викликати помилку для непотрібних стубів
    private UserRepository userRepository;

    @InjectMocks
    private MessageService messageService;

    private User sender;
    private User receiver;
    private Message message;

    @BeforeEach
    void setUp() {
        sender = new User();
        sender.setId(1L);
        sender.setFirstName("John");
        sender.setLastName("Doe");

        receiver = new User();
        receiver.setId(2L);
        receiver.setFirstName("Jane");
        receiver.setLastName("Smith");

        message = new Message();
        message.setId(10L);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setText("Hello");
        message.setRead(false);
        message.setCreatedDate(LocalDateTime.now());
    }

    @Test
    void create_shouldReturnMessageResponse() {
        MessageCreateRequest request = new MessageCreateRequest(2L, "Hello!");

        // Моки для користувачів
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        MessageResponse response = messageService.create(1L, request);

        assertNotNull(response);
        assertEquals("Hello", response.getText());
        assertEquals(1L, response.getSender().getId());
        assertEquals(2L, response.getReceiver().getId());

        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void create_shouldThrowNotFoundException_ifSenderMissing() {
        MessageCreateRequest request = new MessageCreateRequest(2L, "Hello!");

        // Якщо користувач не знайдений
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> messageService.create(1L, request));
    }

    @Test
    void create_shouldThrowNotFoundException_ifReceiverMissing() {
        MessageCreateRequest request = new MessageCreateRequest(2L, "Hello!");

        // Якщо отримувач не знайдений
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> messageService.create(1L, request));
    }

    @Test
    void update_shouldReturnMessageResponse() {
        // Створюємо запит для оновлення повідомлення
        MessageUpdateRequest request = new MessageUpdateRequest("Updated text");

        // Моки для повідомлення та користувачів
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender)); // Перевіряємо, чи існує користувач
        when(messageRepository.findById(10L)).thenReturn(Optional.of(message)); // Перевіряємо, чи існує повідомлення
        when(messageRepository.save(any(Message.class))).thenReturn(message); // Мок для збереження повідомлення

        // Викликаємо метод сервісу
        MessageResponse response = messageService.update(1L, 10L, request);

        // Перевірка результатів
        assertNotNull(response);
        assertEquals("Updated text", response.getText()); // Перевірка, чи текст було оновлено
        verify(messageRepository).save(any(Message.class)); // Перевірка виклику save
    }


    @Test
    void update_shouldThrowNotFoundException_ifMessageMissing() {
        MessageUpdateRequest request = new MessageUpdateRequest("Text");

        // Мок для відсутності повідомлення
        when(messageRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> messageService.update(1L, 999L, request));
    }

    @Test
    void update_shouldThrowNotFoundException_ifUserMissing() {
        MessageUpdateRequest request = new MessageUpdateRequest("Text");

        // Мок для відсутності користувача
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> messageService.update(1L, 10L, request));
    }

    @Test
    void read_shouldReturnMessageResponse() {
        message.setRead(true);
        when(messageRepository.findById(10L)).thenReturn(Optional.of(message));
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        MessageResponse response = messageService.read(10L);

        assertNotNull(response);
        assertTrue(response.isRead());
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void read_shouldThrowNotFoundException_ifMessageMissing() {
        when(messageRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> messageService.read(999L));
    }

    @Test
    void delete_shouldCallRepositoryDelete() {
        when(messageRepository.findById(10L)).thenReturn(Optional.of(message));
        doNothing().when(messageRepository).delete(message);

        messageService.delete(10L);

        verify(messageRepository).delete(message);
    }

    @Test
    void delete_shouldThrowNotFoundException_ifMessageMissing() {
        when(messageRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> messageService.delete(999L));
    }
}

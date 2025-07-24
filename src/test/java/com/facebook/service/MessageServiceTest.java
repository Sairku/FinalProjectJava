package com.facebook.service;

import com.facebook.dto.MessageCreateRequest;
import com.facebook.dto.MessageResponse;
import com.facebook.dto.MessageUpdateRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
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

        messageService.delete(10L, 1L);

        verify(messageRepository).delete(message);
    }

    @Test
    void delete_shouldThrowNotFoundException_ifMessageMissing() {
        when(messageRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> messageService.delete(999L, 1L));
    }

    @Test
    void getMessagesWithFriend_shouldReturnPageOfMessageResponses() {
        int page = 0;
        int size = 2;

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));

        Message secondMessage = new Message();
        secondMessage.setId(11L);
        secondMessage.setSender(receiver);
        secondMessage.setReceiver(sender);
        secondMessage.setText("Hi back");
        secondMessage.setRead(true);
        secondMessage.setCreatedDate(LocalDateTime.now().minusMinutes(1));

        Page<Message> messagesPage = new PageImpl<>(List.of(message, secondMessage));

        when(messageRepository.findConversationBetweenUsers(1L, 2L, PageRequest.of(page, size)))
                .thenReturn(messagesPage);

        Page<MessageResponse> responsePage = messageService.getMessagesWithFriend(1L, 2L, page, size);

        assertNotNull(responsePage);
        assertEquals(2, responsePage.getTotalElements());
        assertEquals("Hello", responsePage.getContent().get(0).getText());
        assertEquals("Hi back", responsePage.getContent().get(1).getText());

        verify(messageRepository).findConversationBetweenUsers(1L, 2L, PageRequest.of(page, size));
    }

    @Test
    void getMessagesWithFriend_shouldThrowNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> messageService.getMessagesWithFriend(1L, 2L, 0, 20));
    }
}

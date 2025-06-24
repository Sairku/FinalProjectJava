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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageResponse create(Long senderId, MessageCreateRequest request) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new NotFoundException("Sender not found"));

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new NotFoundException("Receiver not found"));

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setText(request.getText());
        message.setRead(false);
        message.setCreatedDate(LocalDateTime.now());

        Message saved = messageRepository.save(message);

        return mapToResponse(saved);
    }

    public MessageResponse update(Long userId, Long messageId, MessageUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Отримуємо повідомлення за ID з URL
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        // Перевірка, що повідомлення належить користувачу
        if (!message.getSender().getId().equals(user.getId())) {
            throw new SecurityException("You can only edit your own messages");
        }

        // Оновлення тексту повідомлення
        message.setText(request.getText());

        Message updated = messageRepository.save(message);

        return mapToResponse(updated);
    }



    public MessageResponse read(long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        message.setRead(true);

        Message updated = messageRepository.save(message);

        return mapToResponse(updated);
    }

    public void delete(long id, Long userId) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        if (!message.getSender().getId().equals(userId)) {
            throw new SecurityException("You can only delete your own messages");
        }

        messageRepository.delete(message);
    }

    private MessageResponse mapToResponse(Message message) {
        UserShortDto senderDto = new UserShortDto(
                message.getSender().getId(),
                message.getSender().getFirstName(),
                message.getSender().getLastName()
        );

        UserShortDto receiverDto = new UserShortDto(
                message.getReceiver().getId(),
                message.getReceiver().getFirstName(),
                message.getReceiver().getLastName()
        );

        return new MessageResponse(
                message.getId(),
                senderDto,
                receiverDto,
                message.getText(),
                message.isRead(),
                message.getCreatedDate()
        );
    }
}

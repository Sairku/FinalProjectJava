package com.facebook.service;

import com.facebook.dto.MessageCreateRequest;
import com.facebook.dto.MessageUpdateRequest;
import com.facebook.dto.MessageResponse;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service

public class MessageService {
    private final Map<Long, String> messages = new HashMap<>();
    private long currentId = 1;

    public MessageResponse create(MessageCreateRequest request) {
        long id = currentId++;
        messages.put(id, request.getText());
        return new MessageResponse(id, request.getText());
    }

    public MessageResponse update(MessageUpdateRequest request) {
        if (!messages.containsKey(request.getId())) {
            throw new RuntimeException("Повідомлення не знайдено");
        }
        messages.put(request.getId(), request.getText());
        return new MessageResponse(request.getId(), request.getText());
    }

    public MessageResponse read(long id) {
        String text = messages.get(id);
        if (text == null) {
            throw new RuntimeException("Повідомлення не знайдено");
        }
        return new MessageResponse(id, text);
    }

    public void delete(long id) {
        if (!messages.containsKey(id)) {
            throw new RuntimeException("Повідомлення не знайдено");
        }
        messages.remove(id);
    }
}

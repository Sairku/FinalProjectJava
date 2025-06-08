package com.facebook.controller;

import com.facebook.dto.MessageCreateRequest;
import com.facebook.dto.MessageResponse;
import com.facebook.dto.MessageUpdateRequest;
import com.facebook.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/messages")
@RestController

public class MessageController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/create")
    public MessageResponse create(@Valid @RequestBody MessageCreateRequest request) {
        return messageService.create(request);
    }

    @PutMapping("/edit/{id}")
    public MessageResponse update(@PathVariable long id, @Valid @RequestBody MessageUpdateRequest request) {
        request.setId(id);
        return messageService.update(request);
    }

    @GetMapping("/read/{id}")
    public MessageResponse read(@PathVariable long id) {
        return messageService.read(id);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable long id) {
        messageService.delete(id);
    }
}
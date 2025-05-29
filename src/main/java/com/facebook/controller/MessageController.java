package com.facebook.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/messages")

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
    @PutMapping("/read/{id}")
    public MessageResponse read(@PathVariable long id) {
        return messageService.read(id);
    }
    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable long id) {
        messageService.delete(id);
    }
}
package com.facebook.dto;

import java.time.LocalDateTime;

public class MessageResponse {
    private Long id;
    private UserMessageDTO sender;
    private UserMessageDTO receiver;
    private String text;
    private boolean isRead;
    private LocalDateTime createdDate;

    public MessageResponse() {
    }

    public MessageResponse(Long id, UserMessageDTO sender, UserMessageDTO receiver, String text, boolean isRead, LocalDateTime createdDate) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.isRead = isRead;
        this.createdDate = createdDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserMessageDTO getSender() {
        return sender;
    }

    public void setSender(UserMessageDTO sender) {
        this.sender = sender;
    }

    public UserMessageDTO getReceiver() {
        return receiver;
    }

    public void setReceiver(UserMessageDTO receiver) {
        this.receiver = receiver;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}


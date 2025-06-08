package com.facebook.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public class MessageUpdateRequest {
    @NotNull(message = "ID повідомлення є обов’язковим")
    private Long id;

    @NotBlank(message = "Текст не може бути порожнім")
    private String text;

    public MessageUpdateRequest() {
    }

    public MessageUpdateRequest(Long id, String text) {
        this.id = id;
        this.text = text;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

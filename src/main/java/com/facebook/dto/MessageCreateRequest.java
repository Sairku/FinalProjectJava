package com.facebook.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class MessageCreateRequest {
    @NotBlank(message = "Текст повідомлення не може бути порожнім")
    private String text;

    public MessageCreateRequest() {
    }

    public MessageCreateRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    };
}

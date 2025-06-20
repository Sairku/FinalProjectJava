package com.facebook.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentRequestDto {
    @NotBlank(message = "Comment text is mandatory")
    private String text;
}

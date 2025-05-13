package com.facebook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {
    @NotNull(message = "User id is required")
    private Long userId;

    @NotBlank(message = "Description is required")
    private String description;

    private String imgUrl;
}

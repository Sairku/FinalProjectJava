package com.facebook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {
    @NotNull(message = "User id is required")
    private Long userId;

    @NotBlank(message = "Description is required")
    private String description;

    private List<String> images = new ArrayList<>();
}

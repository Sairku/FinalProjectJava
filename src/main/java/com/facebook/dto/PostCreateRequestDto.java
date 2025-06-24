package com.facebook.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequestDto {

    @NotBlank(message = "Text is required")
    private String text;

    private List<String> images = new ArrayList<>();
}

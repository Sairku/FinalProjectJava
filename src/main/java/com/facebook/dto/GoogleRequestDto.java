package com.facebook.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleRequestDto {
    @NotBlank(message = "ID token is required")
    private String idToken;

    private String email;
    private String firstName;
    private String lastName;
}

package com.facebook.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetDto {
    @NotBlank(message = "Token required")
    private String token;

    @NotBlank(message = "New password required")
    private String newPassword;

    @NotBlank(message = "Confirm password required")
    private String confirmPassword;
}

package com.facebook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleRequestDto {
    @NotBlank(message = "ID token is required")
    private String idToken;

    @Schema(hidden = true)
    private String email;

    @Schema(hidden = true)
    private String firstName;

    @Schema(hidden = true)
    private String lastName;
}

package com.facebook.openapi;

import com.facebook.dto.LoginResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;

public class LoginResponseWrapper {
    @Schema(example = "false")
    public boolean error = false;

    @Schema(example = "User logged in successfully")
    public String message;

    public LoginResponseDto data;
}

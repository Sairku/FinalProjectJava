package com.facebook.openapi;

import com.facebook.dto.UserDetailsDto;
import io.swagger.v3.oas.annotations.media.Schema;

public class UserDetailsWrapper {
    @Schema(example = "false")
    public boolean error = false;

    @Schema(example = "User details retrieved successfully")
    public String message;

    public UserDetailsDto data;
}

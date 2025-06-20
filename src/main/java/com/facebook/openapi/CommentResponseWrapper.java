package com.facebook.openapi;

import com.facebook.dto.CommentResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;

public class CommentResponseWrapper {
    @Schema(example = "false")
    public boolean error = false;

    @Schema(example = "Comment retrieved successfully")
    public String message;

    public CommentResponseDto data;
}

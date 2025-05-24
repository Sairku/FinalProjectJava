package com.facebook.openapi;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard error response")
public class ErrorResponseWrapper {
    @Schema(example = "true")
    public boolean error = true;

    @Schema(example = "Error response")
    public String message;

    @Schema(example = "null")
    public Object data = null;
}

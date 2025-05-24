package com.facebook.openapi;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "NotFound error response")
public class NotFoundResponseWrapper {
    @Schema(example = "true")
    public boolean error = true;

    @Schema(example = "Not found")
    public String message;

    @Schema(example = "null")
    public Object data = null;
}

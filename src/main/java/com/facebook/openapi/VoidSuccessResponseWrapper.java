package com.facebook.openapi;

import io.swagger.v3.oas.annotations.media.Schema;

public class VoidSuccessResponseWrapper {
    @Schema(example = "false")
    public boolean error = false;

    @Schema(example = "Some message")
    public String message;

    @Schema(example = "null")
    public Object data = null;
}

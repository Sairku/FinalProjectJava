package com.facebook.openapi;

import com.facebook.dto.GroupResponse;
import io.swagger.v3.oas.annotations.media.Schema;

public class GroupResponseWrapper {
    @Schema(example = "false")
    public boolean error = false;

    @Schema(example = "Group data retrieved successfully")
    public String message;

    public GroupResponse data;
}

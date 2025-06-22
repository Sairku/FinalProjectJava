package com.facebook.openapi;

import com.facebook.model.Friend;
import io.swagger.v3.oas.annotations.media.Schema;

public class FriendResponseWrapper {
    @Schema(example = "false")
    public boolean error = false;

    @Schema(example = "Friend data retrieved successfully")
    public String message;

    public Friend data;
}

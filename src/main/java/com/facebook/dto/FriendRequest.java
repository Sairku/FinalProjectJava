package com.facebook.dto;

import com.facebook.enums.FriendStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FriendRequest {
    @NotBlank(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Friend ID is required")
    private Long friendId;

    private FriendStatus status;
}

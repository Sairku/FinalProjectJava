package com.facebook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupMemberRequest {
    @Schema(description = "ID of the user to be added", example = "123", required = true)
    @NotNull(message = "User's Id is required")
    @Positive(message = "User's Id must be positive")
    private Long userId;

    @Schema(description = "Membership status. Optional for POST, used only in PUT", example = "APPROVED/REJECTED", required = false)
    private String status = "PENDING";
}

package com.facebook.dto;

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
public class GroupAddRequest {
    @NotNull(message = "Group's Id is required")
    @Positive(message = "Group's Id must be positive")
    private Long groupId;

    @NotNull(message = "User's Id is required")
    @Positive(message = "User's Id must be positive")
    private Long userId;

    @NotNull(message = "Initiator's Id is required")
    @Positive(message = "Initiator's Id must be positive")
    private Long initiatedBy;
}

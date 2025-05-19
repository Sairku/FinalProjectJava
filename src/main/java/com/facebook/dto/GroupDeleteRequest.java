package com.facebook.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupDeleteRequest {
    @NotNull(message = "Owner's Id is required")
    @Positive(message = "Owner's Id must be a positive number")
    private Long ownerId;
}

package com.facebook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupCreateRequest {

    @NotNull(message = "Owner's Id is required")
    @Positive(message = "Owner's Id must be a positive number")
    private Long ownerId;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Group's name must be less than 100 characters")
    private String name;

    @NotNull(message = "isPrivate must be specified")
    private Boolean isPrivate;
}

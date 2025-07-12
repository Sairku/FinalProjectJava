package com.facebook.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupResponse {
    private long id;
    private String name;
    private String description;
    private String imageUrl;
    private String color;
    private boolean isMember;
    private boolean isPrivate;
}

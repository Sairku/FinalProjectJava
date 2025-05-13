package com.facebook.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private UserPostDTO user;
    private String description;
    private String imgUrl;
    private LocalDateTime createdDate;
}
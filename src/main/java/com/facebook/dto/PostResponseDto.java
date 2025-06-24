package com.facebook.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDto {
    private Long id;
    private UserShortDto user;
    private String text;
    private List<String> images = new ArrayList<>();
    private LocalDateTime createdDate;
    private int likesCount;
    private int commentsCount;
    private int repostsCount;
}
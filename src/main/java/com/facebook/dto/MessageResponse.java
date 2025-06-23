package com.facebook.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private Long id;

    private UserShortDto sender;

    private UserShortDto receiver;

    private String text;

    private boolean isRead;

    private LocalDateTime createdDate;
}

package com.example.aiagent.dto;

import lombok.Data;

@Data
public class RenameDTO {
    private Long userId;
    private String conversationId;
    private String newTitle;
}
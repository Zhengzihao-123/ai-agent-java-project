package com.example.aiagent.dto;

import lombok.Data;

@Data
public class ChatMessageDTO {
    private Long userId;
    private String conversationId;
    private String agentRole;
    private String message;
}
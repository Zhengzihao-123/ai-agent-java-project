package com.example.aiagent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.aiagent.entity.ChatHistory;

import java.util.List;

public interface ChatHistoryService extends IService<ChatHistory> {
    String sendMessage(Long userId, String agentRole, String message);
    List<ChatHistory> getHistory(Long userId);
    List<ChatHistory> getHistoryByRole(Long userId, String agentRole);
}

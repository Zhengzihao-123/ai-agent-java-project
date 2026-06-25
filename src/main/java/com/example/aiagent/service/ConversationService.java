package com.example.aiagent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.aiagent.entity.Conversation;

import java.util.List;

public interface ConversationService extends IService<Conversation> {
    Conversation findByConversationId(Long userId, String conversationId);
    Conversation create(Long userId, String conversationId, String title, String agentRole);
    boolean rename(Long userId, String conversationId, String newTitle);
    List<Conversation> listByUserId(Long userId);
    boolean delete(Long userId, String conversationId);
}
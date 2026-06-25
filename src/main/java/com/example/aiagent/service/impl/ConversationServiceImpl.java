package com.example.aiagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aiagent.entity.Conversation;
import com.example.aiagent.mapper.ConversationMapper;
import com.example.aiagent.service.ConversationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ConversationService {

    @Override
    public Conversation findByConversationId(Long userId, String conversationId) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getUserId, userId);
        wrapper.eq(Conversation::getConversationId, conversationId);
        return getOne(wrapper);
    }

    @Override
    public Conversation create(Long userId, String conversationId, String title, String agentRole) {
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setConversationId(conversationId);
        conversation.setTitle(title);
        conversation.setAgentRole(agentRole);
        conversation.setCreateTime(LocalDateTime.now());
        conversation.setUpdateTime(LocalDateTime.now());
        save(conversation);
        return conversation;
    }

    @Override
    public boolean rename(Long userId, String conversationId, String newTitle) {
        Conversation conversation = findByConversationId(userId, conversationId);
        if (conversation != null) {
            conversation.setTitle(newTitle);
            conversation.setUpdateTime(LocalDateTime.now());
            return updateById(conversation);
        }
        return false;
    }

    @Override
    public List<Conversation> listByUserId(Long userId) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getUserId, userId);
        wrapper.orderByDesc(Conversation::getUpdateTime);
        return list(wrapper);
    }

    @Override
    public boolean delete(Long userId, String conversationId) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getUserId, userId);
        wrapper.eq(Conversation::getConversationId, conversationId);
        return remove(wrapper);
    }
}
package com.example.aiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aiagent.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
}
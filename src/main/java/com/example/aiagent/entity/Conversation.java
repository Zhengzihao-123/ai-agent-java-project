package com.example.aiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("conversation")
public class Conversation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String conversationId;
    private String title;
    private String agentRole;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
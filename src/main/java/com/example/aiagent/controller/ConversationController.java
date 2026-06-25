package com.example.aiagent.controller;

import com.example.aiagent.common.Result;
import com.example.aiagent.dto.RenameDTO;
import com.example.aiagent.entity.Conversation;
import com.example.aiagent.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversation")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @GetMapping("/list/{userId}")
    public Result<List<Conversation>> listByUserId(@PathVariable Long userId) {
        List<Conversation> list = conversationService.listByUserId(userId);
        return Result.success(list);
    }

    @PostMapping("/rename")
    public Result<Void> rename(@RequestBody RenameDTO dto) {
        boolean success = conversationService.rename(dto.getUserId(), dto.getConversationId(), dto.getNewTitle());
        if (success) {
            return Result.success(null);
        }
        return Result.error("会话不存在");
    }
}
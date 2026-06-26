package com.example.aiagent.controller;

import com.example.aiagent.common.Result;
import com.example.aiagent.entity.ChatHistory;
import com.example.aiagent.service.ChatHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatHistoryService chatHistoryService;

    @PostMapping("/send")
    public Result<String> sendMessage(@RequestBody Map<String, Object> params) {
        Long userId = Long.parseLong(params.get("userId").toString());
        String agentRole = (String) params.get("agentRole");
        String message = (String) params.get("message");
        String answer = chatHistoryService.sendMessage(userId, agentRole, message);
        return Result.success(answer);
    }

    @GetMapping("/history/{userId}")
    public Result<List<ChatHistory>> getHistory(@PathVariable Long userId) {
        List<ChatHistory> history = chatHistoryService.getHistory(userId);
        return Result.success(history);
    }

    @GetMapping("/history/{userId}/{agentRole}")
    public Result<List<ChatHistory>> getHistoryByRole(@PathVariable Long userId,
                                                      @PathVariable String agentRole) {
        List<ChatHistory> history = chatHistoryService.getHistoryByRole(userId, agentRole);
        return Result.success(history);
    }

    @PostMapping("/send-with-rag")
    public Result<Map<String, Object>> sendMessageWithRAG(@RequestBody Map<String, Object> params) {
        Long userId = Long.parseLong(params.get("userId").toString());
        String conversationId = (String) params.get("conversationId");
        String agentRole = (String) params.get("agentRole");
        String message = (String) params.get("message");
        Map<String, Object> result = chatHistoryService.sendMessageWithRAG(userId, conversationId, agentRole, message);
        return Result.success(result);
    }

}

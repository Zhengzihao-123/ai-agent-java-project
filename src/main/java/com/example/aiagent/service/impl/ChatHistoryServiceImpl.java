package com.example.aiagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aiagent.entity.ChatHistory;
import com.example.aiagent.entity.DocChunk;
import com.example.aiagent.entity.KnowledgeDoc;
import com.example.aiagent.mapper.ChatHistoryMapper;
import com.example.aiagent.service.ChatHistoryService;
import com.example.aiagent.service.DocChunkService;
import com.example.aiagent.service.KnowledgeDocService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    // 从配置文件读取ChatAnywhere配置
    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.base-url}")
    private String baseUrl;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.max-tokens}")
    private Integer maxTokens;

    @Value("${openai.temperature}")
    private Float temperature;

    // Spring Boot自带的HTTP工具，不需要任何额外依赖
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private DocChunkService docChunkService;

    @Autowired
    private KnowledgeDocService knowledgeDocService;

    @Override
    public String sendMessage(Long userId, String agentRole, String message) {
        try {
            // 1. 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            // 2. 构建系统提示词
            String systemPrompt = getSystemPrompt(agentRole);

            // 3. 构建请求体（完全符合OpenAI标准格式）
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", temperature);
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", message)
            ));

            // 4. 发送POST请求到ChatAnywhere API
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/chat/completions",
                    request,
                    String.class
            );

            // 5. 解析返回结果
            JsonNode root = objectMapper.readTree(response.getBody());
            String answer = root.get("choices").get(0).get("message").get("content").asText();

            // 6. 完全保留你原来的保存聊天记录逻辑
            ChatHistory chatHistory = new ChatHistory();
            chatHistory.setUserId(userId);
            chatHistory.setConversationId("default");
            chatHistory.setAgentRole(agentRole);
            chatHistory.setUserMessage(message);
            chatHistory.setAiReply(answer);
            chatHistory.setCreateTime(LocalDateTime.now());
            save(chatHistory);

            // 7. 返回AI回答
            return answer;

        } catch (Exception e) {
            // 8. 异常处理，打印详细错误信息方便排查
            e.printStackTrace();
            return "抱歉，我暂时无法回答，请稍后再试。错误信息：" + e.getMessage();
        }
    }

    @Override
    public Map<String, Object> sendMessageWithRAG(Long userId, String conversationId, String agentRole, String message) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<DocChunk> relevantChunks = docChunkService.searchSimilar(null, message, 5);
            
            StringBuilder context = new StringBuilder();
            Set<Long> usedDocIds = new HashSet<>();
            
            for (DocChunk chunk : relevantChunks) {
                context.append(chunk.getChunkText()).append("\n\n");
                usedDocIds.add(chunk.getDocId());
            }
            
            boolean hasKnowledge = !relevantChunks.isEmpty();
            
            List<Map<String, String>> sources = new ArrayList<>();
            if (hasKnowledge) {
                for (Long docId : usedDocIds) {
                    KnowledgeDoc doc = knowledgeDocService.getById(docId);
                    if (doc != null) {
                        Map<String, String> source = new HashMap<>();
                        source.put("docId", doc.getId().toString());
                        source.put("docName", doc.getDocName());
                        sources.add(source);
                    }
                }
            }
            
            String systemPrompt = getSystemPrompt(agentRole);
            if (hasKnowledge) {
                systemPrompt = "你是一位专业的智能助手。你必须根据以下提供的参考资料回答用户的问题，参考资料是你的知识来源。\n\n" +
                        "参考资料：\n" + context + "\n\n" +
                        "重要要求：\n" +
                        "1. 你必须基于参考资料中的内容进行回答，不要编造信息\n" +
                        "2. 如果参考资料中有相关信息，请直接引用并总结资料中的内容\n" +
                        "3. 如果参考资料中没有相关信息，你可以使用自己的知识回答\n" +
                        "4. 回答要详细、准确，直接提供用户所需的具体内容\n" +
                        "5. 在回答末尾加上【引用来源】部分，列出使用的文档名称\n\n" +
                        systemPrompt;
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", temperature);
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", message)
            ));
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/chat/completions",
                    request,
                    String.class
            );
            
            JsonNode root = objectMapper.readTree(response.getBody());
            String answer = root.get("choices").get(0).get("message").get("content").asText();
            
            ChatHistory chatHistory = new ChatHistory();
            chatHistory.setUserId(userId);
            chatHistory.setConversationId(conversationId);
            chatHistory.setAgentRole(agentRole);
            chatHistory.setUserMessage(message);
            chatHistory.setAiReply(answer);
            chatHistory.setCreateTime(LocalDateTime.now());
            save(chatHistory);
            
            result.put("answer", answer);
            result.put("hasKnowledge", hasKnowledge);
            result.put("sources", sources);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("answer", "抱歉，我暂时无法回答，请稍后再试。错误信息：" + e.getMessage());
            result.put("hasKnowledge", false);
            result.put("sources", new ArrayList<>());
        }
        
        return result;
    }

    @Override
    public List<ChatHistory> getHistory(Long userId) {
        LambdaQueryWrapper<ChatHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatHistory::getUserId, userId);
        wrapper.orderByAsc(ChatHistory::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<ChatHistory> getHistoryByRole(Long userId, String agentRole) {
        LambdaQueryWrapper<ChatHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatHistory::getUserId, userId);
        wrapper.eq(ChatHistory::getAgentRole, agentRole);
        wrapper.orderByAsc(ChatHistory::getCreateTime);
        return list(wrapper);
    }

    /**
     * 根据智能体角色获取对应的系统提示词
     */
    private String getSystemPrompt(String agentRole) {
        return switch (agentRole) {
            case "course" -> "你是一位专业的课程智能体，擅长解答各种学科的课程问题，包括计算机、数学、英语等。回答要清晰易懂，条理分明，适合学生学习。";
            case "training" -> "你是一位专业的职业培训智能体，擅长解答各种职业技能培训问题，包括办公软件、编程技能、职场沟通等。回答要实用，注重实操性。";
            case "cert" -> "你是一位专业的考证智能体，擅长解答各种职业资格证书考试问题，包括计算机等级考试、教师资格证、会计证等。回答要准确，重点突出考点。";
            case "competition" -> "你是一位专业的竞赛智能体，擅长解答各种学科竞赛问题，包括数学竞赛、物理竞赛、编程竞赛等。回答要深入，注重解题思路和方法。";
            default -> "你是一位通用智能体，可以解答各种问题。";
        };
    }
}
package com.example.aiagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aiagent.entity.DocChunk;
import com.example.aiagent.mapper.DocChunkMapper;
import com.example.aiagent.service.DocChunkService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class DocChunkServiceImpl extends ServiceImpl<DocChunkMapper, DocChunk> implements DocChunkService {

    @Override
    public List<DocChunk> searchSimilar(Long docId, String query, int topK) {
        LambdaQueryWrapper<DocChunk> wrapper = new LambdaQueryWrapper<>();
        if (docId != null) {
            wrapper.eq(DocChunk::getDocId, docId);
        }
        List<DocChunk> allChunks = list(wrapper);
        
        if (allChunks.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> keywords = extractKeywords(query);
        
        Map<DocChunk, Integer> scores = new HashMap<>();
        for (DocChunk chunk : allChunks) {
            int score = calculateScore(chunk.getChunkText(), keywords);
            if (score > 0) {
                scores.put(chunk, score);
            }
        }
        
        List<Map.Entry<DocChunk, Integer>> sorted = new ArrayList<>(scores.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        List<DocChunk> result = new ArrayList<>();
        for (int i = 0; i < Math.min(topK, sorted.size()); i++) {
            result.add(sorted.get(i).getKey());
        }
        
        return result;
    }

    private List<String> extractKeywords(String query) {
        List<String> keywords = new ArrayList<>();
        
        Pattern englishPattern = Pattern.compile("[a-zA-Z]+");
        java.util.regex.Matcher englishMatcher = englishPattern.matcher(query);
        while (englishMatcher.find()) {
            keywords.add(englishMatcher.group());
        }

        String chineseText = query.replaceAll("[^\\u4e00-\\u9fa5]", "");
        
        if (!chineseText.isEmpty()) {
            for (char c : chineseText.toCharArray()) {
                keywords.add(String.valueOf(c));
            }
            
            for (int i = 0; i < chineseText.length() - 1; i++) {
                keywords.add(chineseText.substring(i, i + 2));
            }
            
            for (int i = 0; i < chineseText.length() - 2; i++) {
                keywords.add(chineseText.substring(i, i + 3));
            }
        }

        if (keywords.isEmpty()) {
            keywords.add(query);
        }

        return keywords;
    }

    private int calculateScore(String text, List<String> keywords) {
        int score = 0;
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                score += keyword.length();
            }
        }
        return score;
    }
}

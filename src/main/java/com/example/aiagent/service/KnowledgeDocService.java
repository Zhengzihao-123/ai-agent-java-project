package com.example.aiagent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.aiagent.entity.KnowledgeDoc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface KnowledgeDocService extends IService<KnowledgeDoc> {
    KnowledgeDoc upload(Long userId, MultipartFile file);
    Map<String, Object> batchImport(Long userId, String directory);
    boolean delete(Long userId, Long docId);
    List<KnowledgeDoc> listByUserId(Long userId);
}

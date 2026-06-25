package com.example.aiagent.controller;

import com.example.aiagent.common.Result;
import com.example.aiagent.entity.DocChunk;
import com.example.aiagent.entity.KnowledgeDoc;
import com.example.aiagent.service.DocChunkService;
import com.example.aiagent.service.KnowledgeDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    @Autowired
    private KnowledgeDocService knowledgeDocService;

    @Autowired
    private DocChunkService docChunkService;

    @PostMapping("/upload")
    public Result<KnowledgeDoc> upload(@RequestParam("file") MultipartFile file,
                                       @RequestParam("userId") Long userId) {
        try {
            KnowledgeDoc doc = knowledgeDocService.upload(userId, file);
            return Result.success(doc);
        } catch (Exception e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/list/{userId}")
    public Result<List<KnowledgeDoc>> listByUserId(@PathVariable Long userId) {
        return Result.success(knowledgeDocService.listByUserId(userId));
    }

    @DeleteMapping("/{userId}/{docId}")
    public Result<Void> delete(@PathVariable Long userId, @PathVariable Long docId) {
        boolean success = knowledgeDocService.delete(userId, docId);
        if (success) {
            return Result.success(null);
        }
        return Result.error("文档不存在");
    }

    @GetMapping("/search")
    public Result<List<DocChunk>> search(@RequestParam(required = false) Long docId,
                                         @RequestParam String query,
                                         @RequestParam(defaultValue = "5") int topK) {
        return Result.success(docChunkService.searchSimilar(docId, query, topK));
    }

    @PostMapping("/batch-import")
    public Result<Map<String, Object>> batchImport(@RequestParam Long userId,
                                                   @RequestParam String directory) {
        try {
            Map<String, Object> result = knowledgeDocService.batchImport(userId, directory);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("批量导入失败: " + e.getMessage());
        }
    }
}
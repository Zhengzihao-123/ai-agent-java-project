package com.example.aiagent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.aiagent.entity.DocChunk;

import java.util.List;

public interface DocChunkService extends IService<DocChunk> {
    List<DocChunk> searchSimilar(Long docId, String query, int topK);
}

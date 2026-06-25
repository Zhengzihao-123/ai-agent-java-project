package com.example.aiagent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aiagent.entity.DocChunk;
import com.example.aiagent.mapper.DocChunkMapper;
import com.example.aiagent.service.DocChunkService;
import org.springframework.stereotype.Service;

@Service
public class DocChunkServiceImpl extends ServiceImpl<DocChunkMapper, DocChunk> implements DocChunkService {
}

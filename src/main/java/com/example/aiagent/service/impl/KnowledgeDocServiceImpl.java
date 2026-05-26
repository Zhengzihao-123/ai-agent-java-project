package com.example.aiagent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aiagent.entity.KnowledgeDoc;
import com.example.aiagent.mapper.KnowledgeDocMapper;
import com.example.aiagent.service.KnowledgeDocService;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeDocServiceImpl extends ServiceImpl<KnowledgeDocMapper, KnowledgeDoc> implements KnowledgeDocService {
}

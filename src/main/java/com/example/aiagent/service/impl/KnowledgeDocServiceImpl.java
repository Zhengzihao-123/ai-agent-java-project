package com.example.aiagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aiagent.entity.DocChunk;
import com.example.aiagent.entity.KnowledgeDoc;
import com.example.aiagent.mapper.DocChunkMapper;
import com.example.aiagent.mapper.KnowledgeDocMapper;
import com.example.aiagent.service.DocChunkService;
import com.example.aiagent.service.KnowledgeDocService;
import com.example.aiagent.util.FileParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class KnowledgeDocServiceImpl extends ServiceImpl<KnowledgeDocMapper, KnowledgeDoc> implements KnowledgeDocService {

    @Autowired
    private DocChunkService docChunkService;

    private static final String UPLOAD_DIR = "uploads";

    @Override
    public KnowledgeDoc upload(Long userId, MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;
            
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            Path filePath = uploadPath.resolve(newFilename);
            file.transferTo(filePath.toFile());
            
            KnowledgeDoc doc = new KnowledgeDoc();
            doc.setUserId(userId);
            doc.setDocName(originalFilename);
            doc.setDocType(extension.substring(1).toLowerCase());
            doc.setFilePath(filePath.toString());
            doc.setCreateTime(LocalDateTime.now());
            save(doc);
            
            processDocument(doc);
            
            return doc;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> batchImport(Long userId, String directory) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        
        List<File> files = FileParserUtil.getAllFiles(directory);
        for (File file : files) {
            try {
                String originalFilename = file.getName();
                String extension = "";
                if (originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String newFilename = UUID.randomUUID().toString() + extension;
                
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                
                Path filePath = uploadPath.resolve(newFilename);
                Files.copy(file.toPath(), filePath);
                
                KnowledgeDoc doc = new KnowledgeDoc();
                doc.setUserId(userId);
                doc.setDocName(originalFilename);
                doc.setDocType(extension.substring(1).toLowerCase());
                doc.setFilePath(filePath.toString());
                doc.setCreateTime(LocalDateTime.now());
                save(doc);
                
                processDocument(doc);
                successCount++;
            } catch (Exception e) {
                failCount++;
            }
        }
        
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("totalCount", files.size());
        return result;
    }

    @Override
    public boolean delete(Long userId, Long docId) {
        LambdaQueryWrapper<KnowledgeDoc> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeDoc::getId, docId);
        wrapper.eq(KnowledgeDoc::getUserId, userId);
        KnowledgeDoc doc = getOne(wrapper);
        if (doc != null) {
            try {
                Files.deleteIfExists(Paths.get(doc.getFilePath()));
            } catch (IOException e) {
            }
            LambdaQueryWrapper<DocChunk> chunkWrapper = new LambdaQueryWrapper<>();
            chunkWrapper.eq(DocChunk::getDocId, docId);
            docChunkService.remove(chunkWrapper);
            remove(wrapper);
            return true;
        }
        return false;
    }

    @Override
    public List<KnowledgeDoc> listByUserId(Long userId) {
        LambdaQueryWrapper<KnowledgeDoc> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeDoc::getUserId, userId);
        wrapper.orderByDesc(KnowledgeDoc::getCreateTime);
        return list(wrapper);
    }

    private void processDocument(KnowledgeDoc doc) {
        try {
            String content = FileParserUtil.parseFile(new File(doc.getFilePath()));
            int chunkSize = 500;
            int overlap = 50;
            
            for (int i = 0; i < content.length(); i += chunkSize - overlap) {
                int end = Math.min(i + chunkSize, content.length());
                String chunkContent = content.substring(i, end);
                
                DocChunk chunk = new DocChunk();
                chunk.setDocId(doc.getId());
                chunk.setChunkText(chunkContent);
                chunk.setChunkIndex(i / (chunkSize - overlap));
                chunk.setTotalChunks((content.length() + chunkSize - overlap - 1) / (chunkSize - overlap));
                docChunkService.save(chunk);
            }
        } catch (IOException e) {
            throw new RuntimeException("文档处理失败: " + e.getMessage(), e);
        }
    }
}

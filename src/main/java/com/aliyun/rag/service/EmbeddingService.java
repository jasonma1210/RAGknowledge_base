package com.aliyun.rag.service;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.embedding.Embedding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 嵌入服务
 * <p>
 * 提供文本嵌入向量生成服务，支持文本分块的向量化处理
 * 集成LangChain4j的嵌入模型，为语义搜索提供向量支持
 * </p>
 * 
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-09
 */
@Service
public class EmbeddingService {
    
    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    private final EmbeddingModel embeddingModel;

    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /**
     * 将文本转换为嵌入向量
     */
    public Embedding embedText(String text) {
        try {
            TextSegment segment = TextSegment.from(text);
            return embeddingModel.embed(segment).content();
        } catch (Exception e) {
            log.error("文本嵌入失败: {}", e.getMessage(), e);
            throw new RuntimeException("文本嵌入失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量将文本转换为嵌入向量
     */
    public List<Embedding> embedTexts(List<String> texts) {
        try {
            List<TextSegment> segments = texts.stream()
                    .map(TextSegment::from)
                    .collect(Collectors.toList());
            
            return embeddingModel.embedAll(segments).content();
        } catch (Exception e) {
            log.error("批量文本嵌入失败: {}", e.getMessage(), e);
            throw new RuntimeException("批量文本嵌入失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将文本块转换为嵌入向量
     */
    public List<Embedding> embedTextChunks(String[] chunks) {
        return embedTexts(Arrays.asList(chunks));
    }
}
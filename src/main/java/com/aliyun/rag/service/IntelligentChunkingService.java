package com.aliyun.rag.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 智能分块服务
 * <p>
 * 提供基于语义和文档类型的智能分块策略
 * 支持不同文档类型的自适应分块大小和重叠策略
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-01-18
 */
@Service
public class IntelligentChunkingService {

    private static final Logger log = LoggerFactory.getLogger(IntelligentChunkingService.class);

    @Value("${document.chunk.pdf.size:800}")
    private int pdfChunkSize;

    @Value("${document.chunk.pdf.overlap:150}")
    private int pdfOverlap;

    @Value("${document.chunk.markdown.size:1200}")
    private int markdownChunkSize;

    @Value("${document.chunk.markdown.overlap:200}")
    private int markdownOverlap;

    @Value("${document.chunk.docx.size:1000}")
    private int docxChunkSize;

    @Value("${document.chunk.docx.overlap:200}")
    private int docxOverlap;

    @Value("${document.chunk.default.size:1000}")
    private int defaultChunkSize;

    @Value("${document.chunk.default.overlap:200}")
    private int defaultOverlap;

    @Value("${document.intelligent-chunking:true}")
    private boolean intelligentChunkingEnabled;

    /**
     * 文档类型枚举
     */
    public enum DocumentType {
        PDF("pdf", 800, 150),
        MARKDOWN("md", 1200, 200),
        DOCX("docx", 1000, 200),
        TXT("txt", 1000, 200),
        EPUB("epub", 1000, 200);

        private final String extension;
        private final int optimalChunkSize;
        private final int optimalOverlap;

        DocumentType(String extension, int optimalChunkSize, int optimalOverlap) {
            this.extension = extension;
            this.optimalChunkSize = optimalChunkSize;
            this.optimalOverlap = optimalOverlap;
        }

        public String getExtension() {
            return extension;
        }

        public int getOptimalChunkSize() {
            return optimalChunkSize;
        }

        public int getOptimalOverlap() {
            return optimalOverlap;
        }

        public static DocumentType fromExtension(String extension) {
            for (DocumentType type : values()) {
                if (type.extension.equalsIgnoreCase(extension)) {
                    return type;
                }
            }
            return TXT; // 默认类型
        }
    }

    /**
     * 文本块类
     */
    public static class TextChunk {
        private String content;
        private int startIndex;
        private int endIndex;
        private String metadata;
        private double importance;

        public TextChunk(String content, int startIndex, int endIndex) {
            this.content = content;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.importance = calculateImportance(content);
        }

        private double calculateImportance(String content) {
            // 基于内容特征计算重要性分数
            double score = 0.0;
            
            // 标题权重
            if (content.matches("^#{1,6}\\s+.*")) {
                score += 0.3;
            }
            
            // 关键词密度
            String[] keywords = {"重要", "关键", "核心", "主要", "总结", "结论"};
            for (String keyword : keywords) {
                if (content.contains(keyword)) {
                    score += 0.1;
                }
            }
            
            // 长度权重（适中长度更重要）
            int length = content.length();
            if (length > 100 && length < 500) {
                score += 0.2;
            }
            
            return Math.min(score, 1.0);
        }

        // Getters and Setters
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public int getStartIndex() { return startIndex; }
        public void setStartIndex(int startIndex) { this.startIndex = startIndex; }
        public int getEndIndex() { return endIndex; }
        public void setEndIndex(int endIndex) { this.endIndex = endIndex; }
        public String getMetadata() { return metadata; }
        public void setMetadata(String metadata) { this.metadata = metadata; }
        public double getImportance() { return importance; }
        public void setImportance(double importance) { this.importance = importance; }
    }

    /**
     * 智能分块主方法
     */
    public String[] intelligentChunkText(String text, String fileExtension) {
        if (!intelligentChunkingEnabled) {
            return defaultChunking(text, fileExtension);
        }

        DocumentType type = DocumentType.fromExtension(fileExtension);
        List<TextChunk> chunks = semanticChunking(text, type);
        
        return chunks.stream()
                .map(TextChunk::getContent)
                .toArray(String[]::new);
    }

    /**
     * 基于语义的分块
     */
    public List<TextChunk> semanticChunking(String text, DocumentType type) {
        log.info("开始智能分块，文档类型: {}, 文本长度: {}", type, text.length());

        switch (type) {
            case PDF:
                return pdfSemanticChunking(text);
            case MARKDOWN:
                return markdownSemanticChunking(text);
            case DOCX:
                return docxSemanticChunking(text);
            case EPUB:
                return epubSemanticChunking(text);
            default:
                return defaultSemanticChunking(text);
        }
    }

    /**
     * PDF文档语义分块
     */
    private List<TextChunk> pdfSemanticChunking(String text) {
        List<TextChunk> chunks = new ArrayList<>();
        
        // PDF通常按段落分割
        String[] paragraphs = text.split("\\n\\s*\\n");
        StringBuilder currentChunk = new StringBuilder();
        int currentIndex = 0;
        
        for (String paragraph : paragraphs) {
            if (currentChunk.length() + paragraph.length() <= pdfChunkSize) {
                currentChunk.append(paragraph).append("\n\n");
            } else {
                if (currentChunk.length() > 0) {
                    chunks.add(new TextChunk(currentChunk.toString().trim(), 
                            currentIndex, currentIndex + currentChunk.length()));
                    currentIndex += currentChunk.length();
                }
                
                // 处理超长段落
                if (paragraph.length() > pdfChunkSize) {
                    List<TextChunk> subChunks = splitLongParagraph(paragraph, pdfChunkSize, pdfOverlap);
                    chunks.addAll(subChunks);
                } else {
                    currentChunk = new StringBuilder(paragraph).append("\n\n");
                }
            }
        }
        
        if (currentChunk.length() > 0) {
            chunks.add(new TextChunk(currentChunk.toString().trim(), 
                    currentIndex, currentIndex + currentChunk.length()));
        }
        
        return chunks;
    }

    /**
     * Markdown文档语义分块
     */
    private List<TextChunk> markdownSemanticChunking(String text) {
        List<TextChunk> chunks = new ArrayList<>();
        
        // Markdown按标题和段落分割
        String[] sections = text.split("(?=^#{1,6}\\s)");
        
        for (String section : sections) {
            if (section.trim().isEmpty()) continue;
            
            if (section.length() <= markdownChunkSize) {
                chunks.add(new TextChunk(section.trim(), 0, section.length()));
            } else {
                // 按段落进一步分割
                String[] paragraphs = section.split("\\n\\s*\\n");
                StringBuilder currentChunk = new StringBuilder();
                
                for (String paragraph : paragraphs) {
                    if (currentChunk.length() + paragraph.length() <= markdownChunkSize) {
                        currentChunk.append(paragraph).append("\n\n");
                    } else {
                        if (currentChunk.length() > 0) {
                            chunks.add(new TextChunk(currentChunk.toString().trim(), 0, currentChunk.length()));
                        }
                        currentChunk = new StringBuilder(paragraph).append("\n\n");
                    }
                }
                
                if (currentChunk.length() > 0) {
                    chunks.add(new TextChunk(currentChunk.toString().trim(), 0, currentChunk.length()));
                }
            }
        }
        
        return chunks;
    }

    /**
     * DOCX文档语义分块
     */
    private List<TextChunk> docxSemanticChunking(String text) {
        List<TextChunk> chunks = new ArrayList<>();
        
        // DOCX按段落和标题分割
        String[] paragraphs = text.split("\\n");
        StringBuilder currentChunk = new StringBuilder();
        int currentIndex = 0;
        
        for (String paragraph : paragraphs) {
            if (currentChunk.length() + paragraph.length() <= docxChunkSize) {
                currentChunk.append(paragraph).append("\n");
            } else {
                if (currentChunk.length() > 0) {
                    chunks.add(new TextChunk(currentChunk.toString().trim(), 
                            currentIndex, currentIndex + currentChunk.length()));
                    currentIndex += currentChunk.length();
                }
                
                // 添加重叠内容
                String overlapContent = getOverlapContent(currentChunk.toString(), docxOverlap);
                currentChunk = new StringBuilder(overlapContent).append(paragraph).append("\n");
            }
        }
        
        if (currentChunk.length() > 0) {
            chunks.add(new TextChunk(currentChunk.toString().trim(), 
                    currentIndex, currentIndex + currentChunk.length()));
        }
        
        return chunks;
    }

    /**
     * EPUB文档语义分块
     */
    private List<TextChunk> epubSemanticChunking(String text) {
        // EPUB类似HTML结构，按章节分割
        List<TextChunk> chunks = new ArrayList<>();
        
        // 按章节标题分割
        String[] chapters = text.split("(?=第[一二三四五六七八九十\\d]+章|Chapter\\s+\\d+)");
        
        for (String chapter : chapters) {
            if (chapter.trim().isEmpty()) continue;
            
            if (chapter.length() <= defaultChunkSize) {
                chunks.add(new TextChunk(chapter.trim(), 0, chapter.length()));
            } else {
                // 进一步分割
                List<TextChunk> subChunks = splitLongText(chapter, defaultChunkSize, defaultOverlap);
                chunks.addAll(subChunks);
            }
        }
        
        return chunks;
    }

    /**
     * 默认语义分块
     */
    private List<TextChunk> defaultSemanticChunking(String text) {
        return splitLongText(text, defaultChunkSize, defaultOverlap);
    }

    /**
     * 分割长文本
     */
    private List<TextChunk> splitLongText(String text, int chunkSize, int overlap) {
        List<TextChunk> chunks = new ArrayList<>();
        
        String[] sentences = text.split("[.!?。！？]+");
        StringBuilder currentChunk = new StringBuilder();
        int currentIndex = 0;
        
        for (String sentence : sentences) {
            if (currentChunk.length() + sentence.length() <= chunkSize) {
                currentChunk.append(sentence).append(". ");
            } else {
                if (currentChunk.length() > 0) {
                    chunks.add(new TextChunk(currentChunk.toString().trim(), 
                            currentIndex, currentIndex + currentChunk.length()));
                    currentIndex += currentChunk.length();
                    
                    // 添加重叠内容
                    String overlapContent = getOverlapContent(currentChunk.toString(), overlap);
                    currentChunk = new StringBuilder(overlapContent).append(sentence).append(". ");
                } else {
                    // 单个句子过长，强制分割
                    chunks.add(new TextChunk(sentence, currentIndex, currentIndex + sentence.length()));
                    currentIndex += sentence.length();
                }
            }
        }
        
        if (currentChunk.length() > 0) {
            chunks.add(new TextChunk(currentChunk.toString().trim(), 
                    currentIndex, currentIndex + currentChunk.length()));
        }
        
        return chunks;
    }

    /**
     * 分割超长段落
     */
    private List<TextChunk> splitLongParagraph(String paragraph, int chunkSize, int overlap) {
        List<TextChunk> chunks = new ArrayList<>();
        
        for (int i = 0; i < paragraph.length(); i += chunkSize - overlap) {
            int endIndex = Math.min(i + chunkSize, paragraph.length());
            String chunk = paragraph.substring(i, endIndex);
            chunks.add(new TextChunk(chunk, i, endIndex));
        }
        
        return chunks;
    }

    /**
     * 获取重叠内容
     */
    private String getOverlapContent(String text, int overlapSize) {
        if (text.length() <= overlapSize) {
            return text;
        }
        return text.substring(text.length() - overlapSize);
    }

    /**
     * 默认分块方法（保持向后兼容）
     */
    private String[] defaultChunking(String text, String fileExtension) {
        DocumentType type = DocumentType.fromExtension(fileExtension);
        int chunkSize = type.getOptimalChunkSize();
        int overlap = type.getOptimalOverlap();
        
        return splitLongText(text, chunkSize, overlap).stream()
                .map(TextChunk::getContent)
                .toArray(String[]::new);
    }

    /**
     * 计算最优分块大小
     */
    public int calculateOptimalChunkSize(String text, DocumentType type) {
        int baseSize = type.getOptimalChunkSize();
        
        // 根据文本特征调整
        if (text.contains("代码") || text.contains("```")) {
            return (int) (baseSize * 1.2); // 代码内容需要更大的块
        }
        
        if (text.matches(".*[。！？]{3,}.*")) {
            return (int) (baseSize * 0.8); // 短句较多时减小块大小
        }
        
        return baseSize;
    }

    /**
     * 获取分块统计信息
     */
    public Map<String, Object> getChunkingStats(String[] chunks) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalChunks", chunks.length);
        
        if (chunks.length > 0) {
            int totalLength = Arrays.stream(chunks).mapToInt(String::length).sum();
            double avgLength = (double) totalLength / chunks.length;
            
            stats.put("averageChunkLength", avgLength);
            stats.put("totalTextLength", totalLength);
            
            int minLength = Arrays.stream(chunks).mapToInt(String::length).min().orElse(0);
            int maxLength = Arrays.stream(chunks).mapToInt(String::length).max().orElse(0);
            
            stats.put("minChunkLength", minLength);
            stats.put("maxChunkLength", maxLength);
        }
        
        return stats;
    }
}

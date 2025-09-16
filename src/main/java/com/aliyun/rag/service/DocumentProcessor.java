package com.aliyun.rag.service;

import com.aliyun.rag.model.DocumentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

/**
 * 文档处理服务
 * <p>
 * 提供文档内容提取和文本分块功能
 * 支持多种文档格式（PDF、DOCX、TXT、Markdown、EPUB）的内容提取
 * 采用智能分块策略，支持重叠分块以提高检索准确性
 * </p>
 *
 * @author Jason Ma
 * @version 1.0.0
 * @since 2025-09-09
 */
@Service
public class DocumentProcessor {

    private static final Logger log = LoggerFactory.getLogger(DocumentProcessor.class);

    @Value("${document.chunk.size:1000}")
    private int CHUNK_SIZE;

    @Value("${document.chunk.overlap:200}")
    private int CHUNK_OVERLAP;

    private static final Pattern EPUB_PATTERN = Pattern.compile(".*\\.epub$", Pattern.CASE_INSENSITIVE);

    /**
     * 处理上传的文档
     */
    public String processDocument(MultipartFile file, DocumentInfo documentInfo) {
        try {
            String fileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(fileName);

            String content = extractContent(file, fileExtension);

            documentInfo.setFileName(fileName);
            documentInfo.setFileType(fileExtension.toUpperCase());
            documentInfo.setFileSize(file.getSize());

            return content;

        } catch (Exception e) {
            log.error("文档处理失败: {}", e.getMessage(), e);
            throw new RuntimeException("文档处理失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据文件类型提取内容
     */
    private String extractContent(MultipartFile file, String fileExtension) throws Exception {
        return switch (fileExtension.toLowerCase()) {
            case "pdf" -> extractPdfContent(file);
            case "docx" -> extractDocxContent(file);
            case "txt" -> extractTxtContent(file);
            case "md" -> extractMdContent(file);
            case "epub" -> extractEpubContent(file);
            default -> throw new IllegalArgumentException("不支持的文件类型: " + fileExtension);
        };
    }

    /**
     * 提取PDF内容
     */
    private String extractPdfContent(MultipartFile file) throws Exception {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * 提取DOCX内容
     */
    private String extractDocxContent(MultipartFile file) throws Exception {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            return extractor.getText();
        }
    }

    /**
     * 提取TXT内容
     */
    private String extractTxtContent(MultipartFile file) throws Exception {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    /**
     * 提取Markdown内容
     */
    private String extractMdContent(MultipartFile file) throws Exception {
        return extractTxtContent(file); // Markdown也是文本格式
    }

    /**
     * 提取EPUB内容
     */
    private String extractEpubContent(MultipartFile file) throws Exception {
        StringBuilder content = new StringBuilder();

        try (ZipInputStream zip = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.getName().endsWith(".xhtml") || entry.getName().endsWith(".html")) {
                    Document doc = Jsoup.parse(zip, "UTF-8", "");
                    content.append(doc.body().text()).append("\n");
                }
                zip.closeEntry();
            }
        }

        return content.toString();
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    /**
     * 将文本分块
     */
    public String[] chunkText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new String[0];
        }

        // 简单的分块策略：按段落和句子分割
        String[] paragraphs = text.split("\n\n");
        java.util.List<String> chunks = new java.util.ArrayList<>();

        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {
            if (currentChunk.length() + paragraph.length() <= CHUNK_SIZE) {
                currentChunk.append(paragraph).append("\n\n");
            } else {
                if (!currentChunk.isEmpty()) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk = new StringBuilder();

                    // 添加重叠部分
                    if (paragraph.length() > CHUNK_OVERLAP) {
                        currentChunk.append(paragraph, 0, CHUNK_OVERLAP);
                    }
                } else {
                    // 单个段落超过CHUNK_SIZE，按句子分割
                    String[] sentences = paragraph.split("[.!?]+");
                    for (String sentence : sentences) {
                        if (currentChunk.length() + sentence.length() <= CHUNK_SIZE) {
                            currentChunk.append(sentence).append(". ");
                        } else {
                            if (!currentChunk.isEmpty()) {
                                chunks.add(currentChunk.toString().trim());
                                currentChunk = new StringBuilder(sentence).append(". ");
                            } else {
                                chunks.add(sentence);
                            }
                        }
                    }
                }
            }
        }

        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks.toArray(new String[0]);
    }
}
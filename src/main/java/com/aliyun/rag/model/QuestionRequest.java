package com.aliyun.rag.model;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

@Data
public class QuestionRequest {

    private String question;
    private String searchType = "SEMANTIC";
    private Integer maxResults = 10;
    private Double minScore=0.7d;
}

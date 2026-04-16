package com.ai.document.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QueryResponse {
    private String query;
    private String answer;
    private boolean success;
    private Long documentId;
}

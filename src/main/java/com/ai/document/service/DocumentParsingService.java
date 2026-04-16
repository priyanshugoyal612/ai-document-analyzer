package com.ai.document.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class DocumentParsingService {
    
    private static final List<String> SUPPORTED_CONTENT_TYPES = Arrays.asList(
        "application/pdf",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "text/plain"
    );
    
    public boolean isSupportedContentType(String contentType) {
        return SUPPORTED_CONTENT_TYPES.contains(contentType);
    }
    
    public List<String> extractPages(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        
        if (contentType == null || !isSupportedContentType(contentType)) {
            throw new IllegalArgumentException("Unsupported content type: " + contentType);
        }
        
        try (InputStream inputStream = file.getInputStream()) {
            switch (contentType) {
                case "application/pdf":
                    return extractPdfPages(inputStream);
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                    return extractDocxPages(inputStream);
                case "text/plain":
                    return extractTextPages(inputStream);
                default:
                    throw new IllegalArgumentException("Unsupported content type: " + contentType);
            }
        }
    }
    
    public List<String> extractPagesFromFile(File file, String contentType) throws IOException {
        if (contentType == null || !isSupportedContentType(contentType)) {
            throw new IllegalArgumentException("Unsupported content type: " + contentType);
        }
        
        try (FileInputStream inputStream = new FileInputStream(file)) {
            switch (contentType) {
                case "application/pdf":
                    return extractPdfPages(inputStream);
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                    return extractDocxPages(inputStream);
                case "text/plain":
                    return extractTextPages(inputStream);
                default:
                    throw new IllegalArgumentException("Unsupported content type: " + contentType);
            }
        }
    }
    
    private List<String> extractPdfPages(InputStream inputStream) throws IOException {
        List<String> pages = new ArrayList<>();
        
        try (PDDocument document = org.apache.pdfbox.Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            int pageCount = document.getNumberOfPages();
            
            log.debug("Processing PDF with {} pages", pageCount);
            
            for (int i = 1; i <= pageCount; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String pageText = stripper.getText(document).trim();
                
                if (!pageText.isEmpty()) {
                    pages.add(pageText);
                } else {
                    pages.add("[Page " + i + " - No text content]");
                }
            }
        }
        
        return pages;
    }
    
    private List<String> extractDocxPages(InputStream inputStream) throws IOException {
        List<String> pages = new ArrayList<>();
        
        try (XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            
            String fullText = extractor.getText();
            if (fullText == null || fullText.trim().isEmpty()) {
                pages.add("[Document - No text content]");
                return pages;
            }
            
            // Split by form feed characters or approximate page breaks
            String[] potentialPages = fullText.split("\\f|\\n\\s*\\n\\s*\\n");
            
            if (potentialPages.length == 1) {
                // If no clear page breaks, split by approximate page length
                pages.addAll(splitTextIntoPages(fullText));
            } else {
                for (String page : potentialPages) {
                    String trimmedPage = page.trim();
                    if (!trimmedPage.isEmpty()) {
                        pages.add(trimmedPage);
                    }
                }
            }
        }
        
        return pages;
    }
    
    private List<String> extractTextPages(InputStream inputStream) throws IOException {
        List<String> pages = new ArrayList<>();
        
        String content = new String(inputStream.readAllBytes());
        if (content.trim().isEmpty()) {
            pages.add("[Document - No text content]");
            return pages;
        }
        
        // Split by form feed characters or multiple newlines
        String[] potentialPages = content.split("\\f|\\n\\s*\\n\\s*\\n");
        
        if (potentialPages.length == 1) {
            // If no clear page breaks, split by approximate page length
            pages.addAll(splitTextIntoPages(content));
        } else {
            for (String page : potentialPages) {
                String trimmedPage = page.trim();
                if (!trimmedPage.isEmpty()) {
                    pages.add(trimmedPage);
                }
            }
        }
        
        return pages;
    }
    
    private List<String> splitTextIntoPages(String text) {
        List<String> pages = new ArrayList<>();
        
        // Approximate page size (around 2500 characters)
        int pageSize = 2500;
        int totalLength = text.length();
        
        for (int i = 0; i < totalLength; i += pageSize) {
            int end = Math.min(i + pageSize, totalLength);
            
            if (end < totalLength) {
                // Try to break at a sentence or paragraph boundary
                int lastSentence = text.lastIndexOf('.', end);
                int lastParagraph = text.lastIndexOf('\n', end);
                int breakPoint = Math.max(lastSentence, lastParagraph);
                
                if (breakPoint > i) {
                    end = breakPoint + 1;
                }
            }
            
            String page = text.substring(i, end).trim();
            if (!page.isEmpty()) {
                pages.add(page);
            }
        }
        
        return pages;
    }
    
    public String getFileExtension(String contentType) {
        switch (contentType) {
            case "application/pdf":
                return ".pdf";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                return ".docx";
            case "text/plain":
                return ".txt";
            default:
                return ".bin";
        }
    }
}

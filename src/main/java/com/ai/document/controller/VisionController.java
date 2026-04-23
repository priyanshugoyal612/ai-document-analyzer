package com.ai.document.controller;

import com.ai.document.service.VisionDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/vision")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class VisionController {
    
    private final VisionDocumentService visionDocumentService;
    
    /**
     * Analyze an image with a custom query
     */
    @PostMapping("/analyze")
    public ResponseEntity<Map<String, String>> analyzeImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("query") String query) {
        try {
            log.info("Analyzing image: {}, Query: {}", file.getOriginalFilename(), query);
            
            String base64Image = visionDocumentService.encodeToBase64(file.getBytes());
            String mimeType = file.getContentType();
            
            String response = visionDocumentService.analyzeImage(base64Image, mimeType, query);
            
            Map<String, String> result = new HashMap<>();
            result.put("response", response);
            result.put("filename", file.getOriginalFilename());
            
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("Error analyzing image: {}", file.getOriginalFilename(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to process image: " + e.getMessage()));
        }
    }
    
    /**
     * Extract text from an image (OCR)
     */
    @PostMapping("/extract-text")
    public ResponseEntity<Map<String, String>> extractText(
            @RequestParam("file") MultipartFile file) {
        try {
            log.info("Extracting text from image: {}", file.getOriginalFilename());
            
            String base64Image = visionDocumentService.encodeToBase64(file.getBytes());
            String mimeType = file.getContentType();
            
            String extractedText = visionDocumentService.extractTextFromImage(base64Image, mimeType);
            
            Map<String, String> result = new HashMap<>();
            result.put("text", extractedText);
            result.put("filename", file.getOriginalFilename());
            
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("Error extracting text from image: {}", file.getOriginalFilename(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to extract text: " + e.getMessage()));
        }
    }
    
    /**
     * Analyze a chart or graph in an image
     */
    @PostMapping("/analyze-chart")
    public ResponseEntity<Map<String, String>> analyzeChart(
            @RequestParam("file") MultipartFile file) {
        try {
            log.info("Analyzing chart in image: {}", file.getOriginalFilename());
            
            String base64Image = visionDocumentService.encodeToBase64(file.getBytes());
            String mimeType = file.getContentType();
            
            String analysis = visionDocumentService.analyzeChart(base64Image, mimeType);
            
            Map<String, String> result = new HashMap<>();
            result.put("analysis", analysis);
            result.put("filename", file.getOriginalFilename());
            
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("Error analyzing chart: {}", file.getOriginalFilename(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to analyze chart: " + e.getMessage()));
        }
    }
}

package com.ai.document;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AIDocumentAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AIDocumentAnalyzerApplication.class, args);
    }

}

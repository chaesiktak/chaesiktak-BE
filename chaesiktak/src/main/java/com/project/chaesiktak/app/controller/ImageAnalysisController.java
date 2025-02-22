package com.project.chaesiktak.app.controller;

import com.project.chaesiktak.app.service.ImageAnalysisService;
import com.project.chaesiktak.global.dto.ApiResponseTemplete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ImageAnalysisController {

    private final ImageAnalysisService imageAnalysisService;

    @Autowired
    public ImageAnalysisController(ImageAnalysisService imageAnalysisService) {
        this.imageAnalysisService = imageAnalysisService;
    }

    @PostMapping("/analyze-image")
    public ResponseEntity<ApiResponseTemplete<Map<String, Object>>> analyzeImage(@RequestBody Map<String, String> requestBody) {
        return imageAnalysisService.processImageAnalysis(requestBody);
    }
}

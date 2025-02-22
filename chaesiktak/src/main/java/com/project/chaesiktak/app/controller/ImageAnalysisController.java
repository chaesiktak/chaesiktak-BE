package com.project.chaesiktak.app.controller;

import com.project.chaesiktak.app.service.ImageAnalysisService;
import com.project.chaesiktak.app.service.ImageService;
import com.project.chaesiktak.global.dto.ApiResponseTemplete;
import com.project.chaesiktak.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ImageAnalysisController {

    private final ImageService imageService;  // ImageService 주입
    private final ImageAnalysisService imageAnalysisService;

    @Autowired
    public ImageAnalysisController(ImageService imageService, ImageAnalysisService imageAnalysisService) {
        this.imageService = imageService;  // ImageService 초기화
        this.imageAnalysisService = imageAnalysisService;
    }

    // 이미지 업로드 후 분석 처리
    @CrossOrigin(origins = {"http://chaesiktakimgseg.duckdns.org:5000", "http://chaesiktakllm.duckdns.org:5000"})
    @PostMapping("/analyze-image")
    public ResponseEntity<ApiResponseTemplete<Map<String, Object>>> analyzeImage(@RequestParam("image") MultipartFile imageFile) {
        try {
            // 이미지 업로드 (ImageService의 uploadImage 호출)
            String imageUrl = imageService.uploadImage(imageFile);

            // 이미지 분석 서비스 호출
            return imageAnalysisService.processImageAnalysis(imageUrl);
        } catch (Exception e) {
            // 오류 발생 시 에러 응답
            return ApiResponseTemplete.error(ErrorCode.IMAGE_UPLOAD_ERROR, null); // 업로드 오류 처리
        }
    }
}

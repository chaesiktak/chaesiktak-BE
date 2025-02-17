package com.project.chaesiktak.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Service
public class ImageAnalysisService {

    private final RestTemplate restTemplate;

    @Value("${image.server.url}")
    private String IMAGE_SERVER_URL;

    @Value("${llm.server.url}")
    private String LLM_SERVER_URL;

    @Autowired
    public ImageAnalysisService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<?> processImageAnalysis(Map<String, String> requestBody) {
        try {
            // 이미지 분석 서버 요청
            ResponseEntity<Map> imageResponse = restTemplate.postForEntity(IMAGE_SERVER_URL, requestBody, Map.class);

            if (imageResponse.getStatusCode() != HttpStatus.OK || imageResponse.getBody() == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 분석 서버 오류");
            }

            // counts 데이터 추출
            Map<String, Object> imageData = imageResponse.getBody();
            Map<String, Object> counts = (Map<String, Object>) imageData.get("counts");

            if (counts == null || counts.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("counts 데이터 없음");
            }

            // LLM 서버 요청
            ResponseEntity<Map> llmResponse = restTemplate.postForEntity(LLM_SERVER_URL, Collections.singletonMap("counts", counts), Map.class);

            if (llmResponse.getStatusCode() != HttpStatus.OK || llmResponse.getBody() == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("LLM 서버 오류");
            }

            return ResponseEntity.ok(llmResponse.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류: " + e.getMessage());
        }
    }
}


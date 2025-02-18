package com.project.chaesiktak.app.service;

import com.project.chaesiktak.global.dto.ApiResponseTemplete;
import com.project.chaesiktak.global.exception.ErrorCode;
import com.project.chaesiktak.global.exception.SuccessCode;
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
    private final String IMAGE_SERVER_URL;
    private final String LLM_SERVER_URL;

    @Autowired
    public ImageAnalysisService(RestTemplate restTemplate,
                                @Value("${image.server.url}") String imageServerUrl,
                                @Value("${llm.server.url}") String llmServerUrl) {
        this.restTemplate = restTemplate;
        this.IMAGE_SERVER_URL = imageServerUrl;
        this.LLM_SERVER_URL = llmServerUrl;
    }

    public ResponseEntity<ApiResponseTemplete<Map<String, Object>>> processImageAnalysis(Map<String, String> requestBody) {
        try {
            // 이미지 분석 서버 요청
            ResponseEntity<Map> imageResponse = restTemplate.postForEntity(IMAGE_SERVER_URL, requestBody, Map.class);

            if (imageResponse.getStatusCode() != HttpStatus.OK || imageResponse.getBody() == null) {
                return ApiResponseTemplete.error(ErrorCode.IMAGE_SERVER_ERROR, Map.of("error", "이미지 분석 서버 오류"));
            }

            // counts 데이터 추출
            Map<String, Object> imageData = imageResponse.getBody();
            Map<String, Object> counts = (Map<String, Object>) imageData.get("counts");

            if (counts == null || counts.isEmpty()) {
                return ApiResponseTemplete.error(ErrorCode.NO_COUNTS_DATA, Map.of("error", "counts 데이터 없음"));
            }

            // LLM 서버 요청
            ResponseEntity<Map> llmResponse = restTemplate.postForEntity(LLM_SERVER_URL, Collections.singletonMap("counts", counts), Map.class);

            if (llmResponse.getStatusCode() != HttpStatus.OK || llmResponse.getBody() == null) {
                return ApiResponseTemplete.error(ErrorCode.LLM_SERVER_ERROR, Map.of("error", "LLM 서버 오류"));
            }

            return ApiResponseTemplete.success(SuccessCode.ANALYSIS_SUCCESS, llmResponse.getBody());

        } catch (Exception e) {
            return ApiResponseTemplete.error(ErrorCode.INTERNAL_SERVER_ERROR, Map.of("error", "서버 오류: " + e.getMessage()));
        }
    }
}

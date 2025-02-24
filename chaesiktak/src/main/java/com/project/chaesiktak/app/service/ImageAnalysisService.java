package com.project.chaesiktak.app.service;

import com.project.chaesiktak.global.dto.ApiResponseTemplete;
import com.project.chaesiktak.global.exception.ErrorCode;
import com.project.chaesiktak.global.exception.SuccessCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class ImageAnalysisService {

    private final RestTemplate restTemplate;
    private final String IMAGE_SERVER_URL;
    private final String LLM_SERVER_URL;
    private final ImageService imageService;

    @Autowired
    public ImageAnalysisService(RestTemplate restTemplate,
                                @Value("${image.server.url}") String imageServerUrl,
                                @Value("${llm.server.url}") String llmServerUrl,
                                ImageService imageService) {
        this.restTemplate = restTemplate;
        this.IMAGE_SERVER_URL = imageServerUrl;
        this.LLM_SERVER_URL = llmServerUrl;
        this.imageService = imageService;
    }

    public ResponseEntity<ApiResponseTemplete<Map<String, Object>>> processImageAnalysis(String imageUrl) {
        try {
            // 1. 이미지 파일을 imgBB로 업로드하여 URL 획득
            // String imageUrl = imageService.uploadImage(imageFile); // 업로드 URL이 잘 전달됨

            // 2. AI 이미지 분석 서버에 URL 전송
            ResponseEntity<Map> imageResponse = restTemplate.postForEntity(IMAGE_SERVER_URL,
                    Collections.singletonMap("image_url", imageUrl), Map.class);

            if (imageResponse.getStatusCode() != HttpStatus.OK || imageResponse.getBody() == null) {
                return ApiResponseTemplete.error(ErrorCode.IMAGE_SERVER_ERROR, Map.of("error", "이미지 분석 서버 오류"));
            }

            // 3. 응답에서 counts 추출
            Map<String, Object> counts = (Map<String, Object>) imageResponse.getBody().get("counts");
            System.out.println(counts);
            if (counts == null || counts.isEmpty()) {
                return ApiResponseTemplete.error(ErrorCode.NO_IMAGE_URL, Map.of("error", "counts 데이터 없음"));
            }

            // 4. LLM 서버로 counts 데이터 전송
            // LLM 서버로 JSON 형식으로 데이터를 전달
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("counts", counts);

            // 요청 본문을 HttpEntity로 감싸기
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> countsResponse = restTemplate.exchange(LLM_SERVER_URL, HttpMethod.POST, entity, Map.class);

            if (countsResponse.getStatusCode() != HttpStatus.OK || countsResponse.getBody() == null) {
                return ApiResponseTemplete.error(ErrorCode.LLM_SERVER_ERROR, Map.of("error", "LLM 서버 오류"));
            }

            // 5. LLM 서버의 응답을 최종 결과로 반환
            Map<String, Object> resultData = countsResponse.getBody();
            if (resultData == null || resultData.isEmpty()) {
                return ApiResponseTemplete.error(ErrorCode.LLM_SERVER_ERROR, Map.of("error", "LLM 서버 응답 비어 있음"));
            }

            // 분석 결과 반환
            return ApiResponseTemplete.success(SuccessCode.ANALYSIS_SUCCESS, resultData);

        } catch (Exception e) {
            // 예외 처리 시, 구체적인 오류 메시지를 포함하여 반환
            return ApiResponseTemplete.error(ErrorCode.INTERNAL_SERVER_ERROR, Map.of("error", "서버 오류: " + e.getMessage()));
        }
    }
}

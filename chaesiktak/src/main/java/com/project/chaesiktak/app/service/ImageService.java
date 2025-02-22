package com.project.chaesiktak.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Service
public class ImageService {

    @Value("${imgbb.api.key}")
    private String imgbbApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ImageService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public String uploadImage(MultipartFile file) throws IOException {
        // imgBB API 엔드포인트 (API 키를 쿼리 파라미터로 전달)
        String url = "https://api.imgbb.com/1/upload?key=" + imgbbApiKey;

        // 파일을 Base64 인코딩 (imgBB는 Base64 인코딩된 문자열을 요구)
        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());

        // 요청 본문 설정: image와 옵션으로 name 추가
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("image", base64Image);
        body.add("name", file.getOriginalFilename());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        // imgBB API 호출
        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

        // JSON 응답 파싱: {"data": { "url": "https://..." }, ...}
        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode dataNode = root.path("data");
        String imageUrl = dataNode.path("url").asText();

        return imageUrl;
    }
}

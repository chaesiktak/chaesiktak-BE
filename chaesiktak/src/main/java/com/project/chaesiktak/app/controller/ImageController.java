package com.project.chaesiktak.app.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.chaesiktak.global.dto.ApiResponseTemplete;
import com.project.chaesiktak.global.exception.ErrorCode;
import com.project.chaesiktak.global.exception.SuccessCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/image")
public class ImageController {

    @Value("${imgbb.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/upload")
    public ResponseEntity<ApiResponseTemplete<String>> uploadImage(@RequestParam("image") MultipartFile file) {
        String url = "https://api.imgbb.com/1/upload?key=" + apiKey;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // 이미지 파일을 MultipartFile로 처리
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();  // 원본 파일 이름을 그대로 전달
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // API 요청
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            // 이미지 업로드 후 반환된 결과에서 URL 추출
            JsonNode responseBody = new ObjectMapper().readTree(response.getBody());
            String imageUrl = responseBody.path("data").path("url").asText();

            // 성공적인 응답을 ApiResponseTemplete 형식으로 반환
            return ApiResponseTemplete.success(SuccessCode.IMAGE_UPLOAD_SUCCESS, imageUrl);

        } catch (IOException e) {
            // IOException 발생 시 에러 응답
            return ApiResponseTemplete.error(ErrorCode.IMAGE_SERVER_ERROR, null);
        } catch (RestClientException e) {
            // RestClientException 발생 시 에러 응답
            return ApiResponseTemplete.error(ErrorCode.IMAGE_UPLOAD_ERROR, null);
        } catch(Exception e){
            // 일반적인 예외 발생 시 에러 응답
            return ApiResponseTemplete.error(ErrorCode.INVALID_REQUEST, null);
        }
    }
}

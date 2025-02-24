package com.project.chaesiktak.app.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.chaesiktak.global.dto.ApiResponseTemplete;
import com.project.chaesiktak.global.exception.ErrorCode;
import com.project.chaesiktak.global.exception.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "이미지 URL API", description = "이미지를 업로드하고 URL 반환하는 API")
@RestController
@RequestMapping("/api/image")
public class ImageController {

    @Value("${imgbb.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    @Operation(
            summary = "이미지 업로드 후 URL 반환",
            description = "이미지를 업로드한 후 String URL을 제공합니다."
    )
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponseTemplete<String>> uploadImage(
            @Parameter(
                    description = "업로드할 이미지 파일",
                    content = @Content(mediaType = "multipart/form-data",
                            schema = @Schema(type = "string", format = "binary"))
            )
            @RequestParam("image") MultipartFile file) {
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

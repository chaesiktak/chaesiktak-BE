package com.project.chaesiktak.app.controller;

import com.project.chaesiktak.app.service.EmailVerificationService;
import com.project.chaesiktak.global.dto.ApiResponseTemplete;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/verify")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;
    /**
     * 이메일 인증 API : 사용자가 이메일 인증 링크 클릭시 해당 API 호출, 인증 처리.
     */
    @GetMapping("/email")
    public ResponseEntity<ApiResponseTemplete<String>> verifyEmail(@RequestParam String token) {
        boolean isVerified = emailVerificationService.verifyEmail(token).isSuccess();

        if (isVerified) {
            return ResponseEntity.ok(
                    ApiResponseTemplete.<String>builder()
                            .status(200)
                            .success(true)
                            .message("이메일 인증이 완료되었습니다.")
                            .data("이제, 로그인이 가능합니다!")
                            .build()
            );
        } else {
            // 예외처리1. 토큰 만료 혹은 잘못된 토큰 값
            return ResponseEntity.badRequest().body(
                    ApiResponseTemplete.<String>builder()
                            .status(400)
                            .success(false)
                            .message("이메일 인증에 실패하였습니다.")
                            .data("토큰이 만료되었거나 유효하지 않습니다. 다시 요청해주세요.")
                            .build()
            );
        }
    }
}

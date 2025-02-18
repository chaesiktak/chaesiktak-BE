package com.project.chaesiktak.app.controller;

import com.project.chaesiktak.app.dto.user.UserPasswordDto;
import com.project.chaesiktak.app.dto.user.UserPasswordDto.UserPasswordResetDto;
import com.project.chaesiktak.app.service.PasswordService;
import com.project.chaesiktak.global.dto.ApiResponseTemplete;
import com.project.chaesiktak.global.exception.ErrorCode;
import com.project.chaesiktak.global.exception.model.CustomException;
import com.project.chaesiktak.global.security.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/verify")
@RequiredArgsConstructor
public class UserPasswordController {

    private final PasswordService passwordService;
    private final TokenService tokenService;
    /**
     * 비밀번호 변경 API (Access Token 검증 후 변경)
     */
    @Operation(summary = "비밀번호 변경 API (Access 토큰 필요)")
    @PostMapping("/passwordupdate")
    public ResponseEntity<ApiResponseTemplete<String>> changePassword(
            HttpServletRequest request, @RequestBody UserPasswordDto userPasswordDto) {
        // 요청 헤더에서 Access Token 추출
        String accessToken = tokenService.extractAccessToken(request)
                .orElse(null);

        if (accessToken == null || !tokenService.validateToken(accessToken)) {
            return ResponseEntity.status(401).body(
                    ApiResponseTemplete.<String>builder()
                            .status(401)
                            .success(false)
                            .message("인증되지 않은 사용자입니다. (Access Token 없음 또는 유효하지 않음)")
                            .data(null)
                            .build()
            );
        }
        // 토큰에서 이메일 추출 및 검증
        String email = tokenService.extractEmail(accessToken)
                .orElse(null);

        if (email == null || !email.equals(userPasswordDto.getEmail())) {
            return ResponseEntity.status(401).body(
                    ApiResponseTemplete.<String>builder()
                            .status(401)
                            .success(false)
                            .message("인증되지 않은 사용자입니다. (토큰의 이메일 불일치)")
                            .data(null)
                            .build()
            );
        }
        // 비밀번호 변경 실행
        boolean isUpdated = passwordService.changePassword(
                userPasswordDto.getEmail(),
                userPasswordDto.getCurrentPassword(),
                userPasswordDto.getNewPassword()
        );

        if (isUpdated) {
            return ResponseEntity.ok(
                    ApiResponseTemplete.<String>builder()
                            .status(200)
                            .success(true)
                            .message("비밀번호가 성공적으로 변경되었습니다.")
                            .data("새로운 비밀번호로 로그인하세요.")
                            .build()
            );
        } else {
            return ResponseEntity.badRequest().body(
                    ApiResponseTemplete.<String>builder()
                            .status(400)
                            .success(false)
                            .message("비밀번호 변경 실패")
                            .data("현재 비밀번호가 일치하지 않거나, 계정을 찾을 수 없습니다.")
                            .build()
            );
        }
    }
    /**
     * 비밀번호 초기화 (임시 비밀번호 이메일 전송)
     */
    @Operation(summary = "임시 비밀번호 전송 API (토큰 인증 불필요)", security = @SecurityRequirement(name = ""))
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponseTemplete<String>> resetPassword(@RequestBody UserPasswordResetDto dto) {
        try {
            boolean isReset = passwordService.resetPassword(dto.getEmail(), dto.getUserName());

            if (isReset) {
                return ResponseEntity.ok(ApiResponseTemplete.<String>builder()
                        .status(200)
                        .success(true)
                        .message("임시 비밀번호가 이메일로 전송되었습니다.")
                        .data(dto.getEmail())
                        .build());
            } else {
                return ResponseEntity.status(500).body(ApiResponseTemplete.<String>builder()
                        .status(500)
                        .success(false)
                        .message("임시 비밀번호 전송에 실패했습니다.")
                        .data(null)
                        .build());
            }
        } catch (CustomException e) {
            if (e.getErrorCode() == ErrorCode.NOT_FOUND_USER_EXCEPTION) {
                return ResponseEntity.status(401).body(ApiResponseTemplete.<String>builder()
                        .status(401)
                        .success(false)
                        .message("이름 또는 이메일 주소를 확인해주세요.")
                        .data(null)
                        .build());
            }
            return ResponseEntity.status(500).body(ApiResponseTemplete.<String>builder()
                    .status(500)
                    .success(false)
                    .message("서버 오류 발생")
                    .data(null)
                    .build());
        }
    }

}

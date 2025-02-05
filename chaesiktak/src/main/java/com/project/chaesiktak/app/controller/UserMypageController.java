package com.project.chaesiktak.app.controller;

import com.project.chaesiktak.app.dto.user.UserUpdateVeganDto;
import com.project.chaesiktak.app.dto.user.UserUpdateNameDto;
import com.project.chaesiktak.app.dto.user.UserUpdateNicknameDto;
import com.project.chaesiktak.app.service.UserService;
import com.project.chaesiktak.global.dto.ApiResponseTemplete;
import com.project.chaesiktak.global.security.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/verify/mypage")
@RequiredArgsConstructor
public class UserMypageController {

    private final UserService userService;
    private final TokenService tokenService;

    /**
     * 유저 비건 타입 변경
     */
    @PatchMapping("/vegan")
    public ResponseEntity<ApiResponseTemplete<String>> updateVeganType(
            HttpServletRequest request,
            @RequestBody UserUpdateVeganDto dto) {

        String email = tokenService.extractAccessToken(request)
                .flatMap(tokenService::extractEmail)
                .orElse(null);

        if (email == null) {
            return ResponseEntity.status(401).body(
                    ApiResponseTemplete.<String>builder()
                            .status(401)
                            .success(false)
                            .message("인증되지 않은 사용자입니다.")
                            .data(null)
                            .build()
            );
        }

        boolean isUpdated = userService.updateVeganType(email, dto.getVeganType());

        if (isUpdated) {
            return ResponseEntity.ok(
                    ApiResponseTemplete.<String>builder()
                            .status(200)
                            .success(true)
                            .message("비건 타입이 변경되었습니다.")
                            .data(dto.getVeganType().name())
                            .build()
            );
        } else {
            return ResponseEntity.badRequest().body(
                    ApiResponseTemplete.<String>builder()
                            .status(400)
                            .success(false)
                            .message("비건 타입 변경 실패")
                            .data(null)
                            .build()
            );
        }
    }

    /**
     * 유저 이름 변경
     */
    @PatchMapping("/name")
    public ResponseEntity<ApiResponseTemplete<String>> updateUserName(
            HttpServletRequest request,
            @RequestBody UserUpdateNameDto dto) {

        String email = tokenService.extractAccessToken(request)
                .flatMap(tokenService::extractEmail)
                .orElse(null);

        if (email == null) {
            return ResponseEntity.status(401).body(
                    ApiResponseTemplete.<String>builder()
                            .status(401)
                            .success(false)
                            .message("인증되지 않은 사용자입니다.")
                            .data(null)
                            .build()
            );
        }

        boolean isUpdated = userService.updateUserName(email, dto.getUserName());

        if (isUpdated) {
            return ResponseEntity.ok(
                    ApiResponseTemplete.<String>builder()
                            .status(200)
                            .success(true)
                            .message("이름이 변경되었습니다.")
                            .data(dto.getUserName())
                            .build()
            );
        } else {
            return ResponseEntity.badRequest().body(
                    ApiResponseTemplete.<String>builder()
                            .status(400)
                            .success(false)
                            .message("이름 변경 실패")
                            .data(null)
                            .build()
            );
        }
    }

    /**
     * 유저 닉네임 변경
     */
    @PatchMapping("/nickname")
    public ResponseEntity<ApiResponseTemplete<String>> updateUserNickname(
            HttpServletRequest request,
            @RequestBody UserUpdateNicknameDto dto) {

        String email = tokenService.extractAccessToken(request)
                .flatMap(tokenService::extractEmail)
                .orElse(null);

        if (email == null) {
            return ResponseEntity.status(401).body(
                    ApiResponseTemplete.<String>builder()
                            .status(401)
                            .success(false)
                            .message("인증되지 않은 사용자입니다.")
                            .data(null)
                            .build()
            );
        }

        boolean isUpdated = userService.updateUserNickname(email, dto.getUserNickName());

        if (isUpdated) {
            return ResponseEntity.ok(
                    ApiResponseTemplete.<String>builder()
                            .status(200)
                            .success(true)
                            .message("닉네임이 변경되었습니다.")
                            .data(dto.getUserNickName())
                            .build()
            );
        } else {
            return ResponseEntity.badRequest().body(
                    ApiResponseTemplete.<String>builder()
                            .status(400)
                            .success(false)
                            .message("닉네임 변경 실패")
                            .data(null)
                            .build()
            );
        }
    }
}

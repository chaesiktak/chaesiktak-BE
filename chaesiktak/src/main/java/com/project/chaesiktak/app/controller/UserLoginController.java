package com.project.chaesiktak.app.controller;

import com.project.chaesiktak.app.dto.user.LoginRequestDto;
import com.project.chaesiktak.app.dto.user.LoginResponseDto;
import com.project.chaesiktak.app.repository.UserRepository;
import com.project.chaesiktak.app.service.LoginService;
import com.project.chaesiktak.global.security.TokenService;
import com.project.chaesiktak.global.dto.ApiResponseTemplete;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class UserLoginController {

    private final LoginService loginService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Operation(summary = "로그인 API (토큰 인증 불필요)", security = @SecurityRequirement(name = ""))
    @PostMapping("/login")
    public ResponseEntity<ApiResponseTemplete<LoginResponseDto>> login(@RequestBody LoginRequestDto loginRequestDto) {

        // 예외처리1. 아이디 또는 비밀번호 필드가 비어있거나, 올바르지 않은 형식인 경우
        if (loginRequestDto.getEmail() == null || loginRequestDto.getEmail().isBlank() ||
                loginRequestDto.getPassword() == null || loginRequestDto.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(
                    ApiResponseTemplete.<LoginResponseDto>builder()
                            .status(401)
                            .success(false)
                            .message("아이디 또는 비밀번호를 확인해주세요.")
                            .data(null)
                            .build()
            );
        }

        // 예외처리2. 아이디(이메일)가 존재 하지 않는 경우
        if (userRepository.findByEmail(loginRequestDto.getEmail()).isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponseTemplete.<LoginResponseDto>builder()
                            .status(401)
                            .success(false)
                            .message("아이디 또는 비밀번호를 확인해주세요.")
                            .data(null)
                            .build()
            );
        }

        // 예외처리3. 비밀번호 불일치
        UserDetails userDetails = loginService.loadUserByUsername(loginRequestDto.getEmail());
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), userDetails.getPassword())) {
            return ResponseEntity.badRequest().body(
                    ApiResponseTemplete.<LoginResponseDto>builder()
                            .status(401)
                            .success(false)
                            .message("아이디 또는 비밀번호를 확인해주세요.")
                            .data(null)
                            .build()
            );
        }

        // 예외처리4. 이메일 주소 미인증 상태
        boolean isEmailVerified = loginService.isEmailVerified(loginRequestDto.getEmail());
        if (!isEmailVerified) {
            return ResponseEntity.status(403).body(
                    ApiResponseTemplete.<LoginResponseDto>builder()
                            .status(403)
                            .success(false)
                            .message("이메일 인증이 필요합니다.")
                            .data(null)
                            .build()
            );
        }

        String accessToken = tokenService.createAccessToken(loginRequestDto.getEmail());
        String refreshToken = tokenService.createRefreshToken();
        tokenService.updateRefreshToken(loginRequestDto.getEmail(), refreshToken);

        LoginResponseDto loginResponse = LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(loginRequestDto.getEmail())
                .build();

        // 정상응답처리 : 로그인 성공!
        return ResponseEntity.ok(ApiResponseTemplete.<LoginResponseDto>builder()
                .status(200)
                .success(true)
                .message("로그인 성공!")
                .data(loginResponse)
                .build());
    }
    /**
     * 로그아웃 API
     */
    @Operation(summary = "로그아웃 API (Access 토큰 필요)")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseTemplete<String>> logout(HttpServletRequest request, HttpServletResponse response) {

        // 요청에서 액세스 토큰 추출
        String accessToken = tokenService.extractAccessToken(request)
                .orElse(null);

        if (accessToken == null || !tokenService.validateToken(accessToken)) {
            return ResponseEntity.status(401).body(
                    ApiResponseTemplete.<String>builder()
                            .status(401)
                            .success(false)
                            .message("인증되지 않은 사용자입니다. (유효하지 않은 액세스 토큰)")
                            .data(null)
                            .build()
            );
        }

        // 토큰에서 사용자 이메일 추출
        String email = tokenService.extractEmail(accessToken)
                .orElse(null);

        if (email == null) {
            return ResponseEntity.status(401).body(
                    ApiResponseTemplete.<String>builder()
                            .status(401)
                            .success(false)
                            .message("인증되지 않은 사용자입니다. (토큰에서 이메일 추출 실패)")
                            .data(null)
                            .build()
            );
        }

        // 해당 사용자의 Refresh Token 삭제
        tokenService.removeRefreshToken(email);

        // 클라이언트 쿠키/헤더에서 토큰 제거
        response.setHeader("Authorization", "");
        response.setHeader("Refresh-Token", "");

        return ResponseEntity.ok(
                ApiResponseTemplete.<String>builder()
                        .status(200)
                        .success(true)
                        .message("로그아웃 성공")
                        .data(null)
                        .build()
        );
    }
}

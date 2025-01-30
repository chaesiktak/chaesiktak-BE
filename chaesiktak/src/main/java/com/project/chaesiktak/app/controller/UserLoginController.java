package com.project.chaesiktak.app.controller;

import com.project.chaesiktak.app.dto.user.LoginRequestDto;
import com.project.chaesiktak.app.dto.user.LoginResponseDto;
import com.project.chaesiktak.app.service.LoginService;
import com.project.chaesiktak.global.security.TokenService;
import com.project.chaesiktak.global.dto.ApiResponseTemplete;
import com.project.chaesiktak.global.exception.ErrorCode;
import com.project.chaesiktak.global.exception.model.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    /**
     * 로그인 API: email,password 필요. /
     * 요구 조건 : 이메일 인증이 완료된 사용자만 로그인 가능
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponseTemplete<LoginResponseDto>> login(@RequestBody LoginRequestDto loginRequestDto) {
        log.info("로그인 시도: {}", loginRequestDto.getEmail()); // log용도
        // 사용자 인증
        UserDetails userDetails = loginService.loadUserByUsername(loginRequestDto.getEmail());
        log.info("유저 정보 로드 완료: {}", userDetails.getUsername()); // log용도

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), userDetails.getPassword())) {
            log.error("비밀번호 불일치: {}", loginRequestDto.getEmail()); //log
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH_EXCEPTION, "비밀번호가 일치하지 않습니다.");
        }

        // 이메일 인증 여부 확인
        boolean isEmailVerified = loginService.isEmailVerified(loginRequestDto.getEmail());
        log.info("이메일 인증 여부 확인: {} - {}", loginRequestDto.getEmail(), isEmailVerified); //log
        if (!isEmailVerified) {
            log.error("이메일 인증 필요: {}", loginRequestDto.getEmail()); //log
            throw new CustomException(ErrorCode.UNAUTHORIZED_EMAIL_EXCEPTION, "이메일 인증이 필요합니다.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword())
        );

        log.info("Authentication 성공: {}", authentication.getName()); //log

        // 토큰 생성 및 저장
        String accessToken = tokenService.createAccessToken(loginRequestDto.getEmail());
        String refreshToken = tokenService.createRefreshToken();
        tokenService.updateRefreshToken(loginRequestDto.getEmail(), refreshToken);

        LoginResponseDto loginResponse = LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(loginRequestDto.getEmail())
                .build();

        return ResponseEntity.ok(ApiResponseTemplete.<LoginResponseDto>builder()
                .status(200)
                .success(true)
                .message("로그인 성공")
                .data(loginResponse)
                .build());
    }
}

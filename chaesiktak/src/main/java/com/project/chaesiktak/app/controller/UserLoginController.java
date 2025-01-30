package com.project.chaesiktak.app.controller;

import com.project.chaesiktak.app.dto.user.LoginRequestDto;
import com.project.chaesiktak.app.dto.user.LoginResponseDto;
import com.project.chaesiktak.app.repository.UserRepository;
import com.project.chaesiktak.app.service.LoginService;
import com.project.chaesiktak.global.security.TokenService;
import com.project.chaesiktak.global.dto.ApiResponseTemplete;
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
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<ApiResponseTemplete<LoginResponseDto>> login(@RequestBody LoginRequestDto loginRequestDto) {
        // 예외처리1. 아이디 또는 비밀번호 필드가 비어있거나, 올바르지 않은 형식인 경우
        if (loginRequestDto.getEmail() == null || loginRequestDto.getEmail().isBlank() ||
                loginRequestDto.getPassword() == null || loginRequestDto.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(
                    ApiResponseTemplete.<LoginResponseDto>builder()
                            .status(400)
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
                            .status(400)
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
                            .status(400)
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

        Authentication authentication = authenticationManager.authenticate( new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword()) );
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
}

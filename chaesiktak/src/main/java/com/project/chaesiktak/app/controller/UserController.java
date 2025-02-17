package com.project.chaesiktak.app.controller;

import com.project.chaesiktak.app.dto.user.UserSignUpDto;
import com.project.chaesiktak.app.service.SignUpService;
import com.project.chaesiktak.global.dto.ApiResponseTemplete;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final SignUpService signUpService;

    /**
     * 회원가입 API : email, password, userName, userNickName 필요.
     */
    @Operation(summary = "회원가입 API (토큰 인증 불필요)", security = @SecurityRequirement(name = ""))
    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponseTemplete<UserSignUpDto>> signUp(@RequestBody UserSignUpDto userSignUpDto) {

        // 공백 체크: 이메일, 비밀번호, 이름, 닉네임
        if (userSignUpDto.getEmail() == null || userSignUpDto.getEmail().trim().isEmpty() ||
                userSignUpDto.getPassword() == null || userSignUpDto.getPassword().trim().isEmpty() ||
                userSignUpDto.getUserName() == null || userSignUpDto.getUserName().trim().isEmpty() ||
                userSignUpDto.getUserNickName() == null || userSignUpDto.getUserNickName().trim().isEmpty()) {

            return ResponseEntity.badRequest().body(
                    ApiResponseTemplete.<UserSignUpDto>builder()
                            .status(400)
                            .success(false)
                            .message("회원가입 필수 정보를 확인해주세요. (이메일, 비밀번호, 이름, 닉네임은 공백이 될 수 없습니다.)")
                            .data(null)
                            .build()
            );
        }

        // 서비스 로직 실행
        ApiResponseTemplete<UserSignUpDto> data = signUpService.signUp(userSignUpDto);
        return ResponseEntity.status(data.getStatus()).body(data);
    }
}

package com.project.chaesiktak.controller;

import com.project.chaesiktak.dto.LoginDTO;
import com.project.chaesiktak.service.LoginService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO loginDTO) {
        try {
            String jwtToken = loginService.login(loginDTO);
            return ResponseEntity.ok(jwtToken);  // 로그인 성공 시 JWT 토큰 반환
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());  // 이메일 인증이 필요함 메시지
        }
    }
}

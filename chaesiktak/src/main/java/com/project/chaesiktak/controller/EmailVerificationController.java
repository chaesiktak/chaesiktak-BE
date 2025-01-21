package com.project.chaesiktak.controller;

import com.project.chaesiktak.service.EmailVerificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    public EmailVerificationController(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    @GetMapping("/verify")
    public String verifyEmail(@RequestParam String token) {
        boolean isVerified = emailVerificationService.verifyEmail(token);
        if (isVerified) {
            return "이메일 인증이 완료되었습니다!";
        } else {
            return "유효하지 않거나 만료된 토큰입니다.";
        }
    }
}

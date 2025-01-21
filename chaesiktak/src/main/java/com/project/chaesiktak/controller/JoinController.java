package com.project.chaesiktak.controller;

import com.project.chaesiktak.dto.JoinDTO;
import com.project.chaesiktak.service.JoinService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/join")
public class JoinController {

    private final JoinService joinService;

    public JoinController(JoinService joinService) {
        this.joinService = joinService;
    }

    @PostMapping
    public ResponseEntity<String> joinProcess(@RequestBody JoinDTO joinDTO) {
        try {
            // 회원가입 처리 (이메일 인증 포함)
            joinService.joinProcess(joinDTO);
            return ResponseEntity.ok("회원가입이 완료되었습니다. 이메일 인증을 완료해주세요.");
        } catch (IllegalArgumentException e) {
            // 예외 처리 (중복된 사용자 또는 잘못된 데이터)
            return ResponseEntity.badRequest().body("회원가입 실패: " + e.getMessage());
        } catch (Exception e) {
            // 일반적인 오류 처리
            e.printStackTrace();
            return ResponseEntity.status(500).body("회원가입 중 오류가 발생했습니다.");
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String verificationCode) {
        try {
            // 이메일 인증 처리
            joinService.verifyEmail(verificationCode);
            return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            // 예외 처리 (잘못된 인증 코드)
            return ResponseEntity.badRequest().body("이메일 인증 실패: " + e.getMessage());
        } catch (Exception e) {
            // 일반적인 오류 처리
            e.printStackTrace();
            return ResponseEntity.status(500).body("이메일 인증 중 오류가 발생했습니다.");
        }
    }

}

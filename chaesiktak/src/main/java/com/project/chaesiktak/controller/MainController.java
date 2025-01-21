package com.project.chaesiktak.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    @GetMapping("/")
    public String mainPage() {
        // 현재 로그인된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            // 로그인된 사용자 정보에서 닉네임 가져오기 (닉네임은 UserDetails에 포함되어 있다고 가정)
            String username = authentication.getName(); // 로그인한 사용자명 (UserEntity의 username 필드)
            return "로그인된 사용자: " + username; // 닉네임을 표시하거나 더 많은 정보를 추가 가능
        } else {
            // 로그인 안 된 경우, 로그인 링크 제공
            return "로그인 상태가 아닙니다. <a href='/login'>로그인</a>";
        }
    }
}


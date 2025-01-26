package com.project.chaesiktak.service;

import com.project.chaesiktak.dto.JoinDTO;
import com.project.chaesiktak.entity.UserEntity;
import com.project.chaesiktak.jwt.JWTUtil;
import com.project.chaesiktak.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class JoinService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailVerificationService emailVerificationService;
    private final JWTUtil jwtUtil;

    public JoinService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, EmailVerificationService emailVerificationService, JWTUtil jwtUtil) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.emailVerificationService = emailVerificationService;
        this.jwtUtil = jwtUtil;
    }

    public String joinProcess(JoinDTO joinDTO) {
        String username = joinDTO.getUsername();
        String password = joinDTO.getPassword();
        String name = joinDTO.getName();
        String nickname = joinDTO.getNickname();

        // 사용자 중복 체크
        if (userRepository.existsByUsername(username)) {
            return "이미 사용 중인 아이디입니다."; // 중복된 사용자 이름 처리
        }

        // 새로운 사용자 생성
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setName(name);
        user.setNickname(nickname);
        user.setRole("ROLE_USER");
        if ("chaesiktak2025@gmail.com".equals(joinDTO.getUsername())) {
            user.setRole("ROLE_ADMIN"); // 특정 이메일은 ROLE_ADMIN으로 설정
        }
        user.setEmailVerified(false); // 이메일 인증이 되지 않은 상태로 설정

        // 사용자 저장
        userRepository.save(user);

        // 이메일 인증 메일 전송
        try {
            emailVerificationService.sendVerificationEmail(username);
        } catch (Exception e) {
            // 이메일 전송 오류 처리
            return "이메일 인증 메일 전송에 실패했습니다. 다시 시도해 주세요.";
        }

        return "회원가입이 완료되었습니다. 이메일을 확인하여 인증을 완료하세요.";
    }
}

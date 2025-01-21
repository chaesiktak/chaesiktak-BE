package com.project.chaesiktak.service;

import com.project.chaesiktak.dto.JoinDTO;
import com.project.chaesiktak.entity.UserEntity;
import com.project.chaesiktak.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class JoinService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailVerificationService emailVerificationService;

    public JoinService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, EmailVerificationService emailVerificationService) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.emailVerificationService = emailVerificationService;
    }

    public void joinProcess(JoinDTO joinDTO) throws MessagingException {

        String username = joinDTO.getUsername();
        String password = joinDTO.getPassword();
        String email = joinDTO.getEmail();
        String name = joinDTO.getName();
        String nickname = joinDTO.getNickname();

        Boolean isExist = userRepository.existsByUsername(username);

        if (isExist) {
            throw new IllegalArgumentException("이미 존재하는 사용자입니다.");
        }

        UserEntity data = new UserEntity();
        data.setUsername(username);
        data.setPassword(bCryptPasswordEncoder.encode(password));
        data.setEmail(email);
        data.setName(name);
        data.setNickname(nickname);
        data.setRole(Objects.equals(username, "admin") ? "ROLE_ADMIN" : "ROLE_USER");
        userRepository.save(data);

        emailVerificationService.sendVerificationEmail(email);
    }
    public void verifyEmail(String verificationCode) {
        boolean isVerified = emailVerificationService.verifyEmail(verificationCode);

        if (!isVerified) {
            throw new IllegalArgumentException("잘못된 인증 코드입니다.");
        }
    }
}

package com.project.chaesiktak.app.service;

import com.project.chaesiktak.app.domain.User;
import com.project.chaesiktak.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 비밀번호 변경 (리프레시 토큰 검증 후 실행)
     */
    public boolean changePassword(String email, String currentPassword, String newPassword) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return false; // 유저가 존재하지 않음
        }

        User user = optionalUser.get();

        // 현재 비밀번호가 일치하는지 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false; // 비밀번호 불일치
        }

        // 새로운 비밀번호를 암호화하여 저장
        user.setEncodedPassword(passwordEncoder, newPassword);
        userRepository.save(user);

        return true;
    }
}

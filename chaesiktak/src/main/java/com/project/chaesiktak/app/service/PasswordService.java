package com.project.chaesiktak.app.service;

import com.project.chaesiktak.app.domain.User;
import com.project.chaesiktak.app.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    /**
     * 비밀번호 변경 (Access Token 검증 후 실행)
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
    /**
     * 임시 비밀번호 발급 및 이메일 전송
     */
    @Transactional
    public boolean resetPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다."));
        // 8자리 임시 비밀번호 생성
        String tempPassword = generateTemporaryPassword();
        // 새로운 비밀번호를 암호화하여 저장
        user.setEncodedPassword(passwordEncoder, tempPassword);
        userRepository.save(user);
        // 이메일로 임시 비밀번호 전송
        return sendTemporaryPasswordByEmail(email, tempPassword);
    }
    /**
     * 8자리 숫자로 임시 비밀번호 생성
     */
    private String generateTemporaryPassword() {
        Random random = new Random();
        StringBuilder tempPassword = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            tempPassword.append(random.nextInt(10)); // 0~9까지 숫자 랜덤 생성
        }
        return tempPassword.toString();
    }

    /**
     * 이메일로 임시 비밀번호 전송
     */
    private boolean sendTemporaryPasswordByEmail(String email, String tempPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject("임시 비밀번호 발급 안내");
            helper.setText("<p>임시 비밀번호가 발급되었습니다.</p>" +
                    "<p>로그인 후 비밀번호를 변경해주세요.</p>" +
                    "<h3>" + tempPassword + "</h3>", true);

            mailSender.send(message);
            return true;
        } catch (MessagingException e) {
            throw new IllegalStateException("이메일 전송 실패", e);
        }
    }
}

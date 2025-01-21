package com.project.chaesiktak.service;

import com.project.chaesiktak.entity.EmailVerificationEntity;
import com.project.chaesiktak.entity.UserEntity;
import com.project.chaesiktak.repository.EmailVerificationRepository;
import com.project.chaesiktak.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.auth-code-expiration-millis}")
    private Long expirationMillis;

    public EmailVerificationService(EmailVerificationRepository emailVerificationRepository, UserRepository userRepository, JavaMailSender mailSender) {
        this.emailVerificationRepository = emailVerificationRepository;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String email) throws MessagingException {
        String token = UUID.randomUUID().toString();
        EmailVerificationEntity entity = new EmailVerificationEntity();
        entity.setEmail(email);
        entity.setToken(token);
        entity.setExpirationTime(LocalDateTime.now().plus(Duration.ofMillis(expirationMillis))); // 수정된 부분
        emailVerificationRepository.save(entity);

        String verificationLink = "http://localhost:8080/verify?token=" + token;

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(email);
        helper.setSubject("이메일 인증");
        helper.setText("<p>이메일 인증을 위해 아래 링크를 클릭해주세요:</p><a href=\"" + verificationLink + "\">인증하기</a>", true);

        mailSender.send(message);
    }

    public boolean verifyEmail(String token) {
        Optional<EmailVerificationEntity> optionalEntity = emailVerificationRepository.findByToken(token);

        if (optionalEntity.isEmpty()) {
            return false;
        }

        EmailVerificationEntity entity = optionalEntity.get();
        if (entity.getExpirationTime().isBefore(LocalDateTime.now())) {
            emailVerificationRepository.delete(entity);
            return false;
        }

        UserEntity user = userRepository.findByEmail(entity.getEmail());
        if (user != null) {
            user.setEmailVerified(true); // 이메일 인증 완료, 이메일 인증 상태를 true로 설정
            userRepository.save(user);  // 사용자 정보 업데이트
        } else {
            // 사용자 없음 처리
            return false;
        }

        emailVerificationRepository.delete(entity);
        return true;
    }
}

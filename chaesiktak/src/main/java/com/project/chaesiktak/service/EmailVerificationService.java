package com.project.chaesiktak.service;

import com.project.chaesiktak.entity.EmailVerificationEntity;
import com.project.chaesiktak.entity.UserEntity;
import com.project.chaesiktak.repository.EmailVerificationRepository;
import com.project.chaesiktak.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.auth-code-expiration-millis}")
    private Long expirationMillis;

    @Autowired
    public EmailVerificationService(EmailVerificationRepository emailVerificationRepository,
                                    UserRepository userRepository,
                                    JavaMailSender mailSender) {
        this.emailVerificationRepository = emailVerificationRepository;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
    }

    @Transactional
    public void sendVerificationEmail(String email) {
        try {
            String token = UUID.randomUUID().toString();
            EmailVerificationEntity entity = new EmailVerificationEntity();
            entity.setUsername(email);
            entity.setToken(token);

            // LocalDateTime을 Date로 변환 (plusMillis 대신 Duration 사용)
            LocalDateTime expirationTime = LocalDateTime.now().plus(Duration.ofMillis(expirationMillis));
            entity.setExpirationTime(Date.from(expirationTime.atZone(ZoneId.systemDefault()).toInstant()));

            emailVerificationRepository.save(entity);

            String verificationLink = "http://localhost:8080/verify?token=" + token;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject("이메일 인증");
            helper.setText("<p>이메일 인증을 위해 아래 링크를 클릭해주세요:</p><a href=\"" + verificationLink + "\">인증하기</a>", true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send verification email.", e);
        }
    }

    public boolean verifyEmail(String token) {
        Optional<EmailVerificationEntity> optionalEntity = emailVerificationRepository.findByToken(token);

        if (optionalEntity.isEmpty()) {
            throw new IllegalArgumentException("Invalid or expired token.");
        }

        EmailVerificationEntity verificationEntity = optionalEntity.get();

        // 토큰 만료 여부 확인
        if (verificationEntity.getExpirationTime().before(new Date())) {
            emailVerificationRepository.deleteById(verificationEntity.getId());
            throw new IllegalArgumentException("Token has expired.");
        }

        // 사용자 이메일 인증 상태 업데이트
        UserEntity userEntity = userRepository.findByUsername(verificationEntity.getUsername());
        if (userEntity == null) {
            throw new IllegalArgumentException("User not found.");
        }

        if (userEntity.getEmailVerified()) {
            throw new IllegalArgumentException("Email is already verified.");
        }

        userEntity.setEmailVerified(true);
        userRepository.save(userEntity);

        // 인증 엔티티 삭제
        emailVerificationRepository.deleteById(verificationEntity.getId());
        return true;
    }
}
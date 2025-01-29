package com.project.chaesiktak.app.service;

import com.project.chaesiktak.app.domain.EmailVerification;
import com.project.chaesiktak.app.domain.User;
import com.project.chaesiktak.app.repository.EmailVerificationRepository;
import com.project.chaesiktak.app.repository.UserRepository;
import com.project.chaesiktak.global.dto.ApiResponseTemplete;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
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

    /**
     * 이메일 인증 메일 발송
     */
    @Transactional
    public void sendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일을 가진 사용자를 찾을 수 없습니다. : " + email));

        String token = UUID.randomUUID().toString();
        LocalDateTime expirationTime = LocalDateTime.now().plus(Duration.ofMillis(expirationMillis));

        EmailVerification emailVerification = EmailVerification.builder()
                .user(user)
                .token(token)
                .expirationTime(Date.from(expirationTime.atZone(ZoneId.systemDefault()).toInstant()))
                .build();

        emailVerificationRepository.save(emailVerification);
        // 메일로 발송하는 인증 링크 양식 정의
        String verificationLink = "http://localhost:8080/api/verify/email?token=" + token;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject("채식탁 서비스 이메일 인증 절차 안내");
            helper.setText("<p>채식탁 서비스의 이메일 인증을 위해 하단의 링크를 클릭해주세요! :</p><a href=\"" + verificationLink + "\">인증하기</a>", true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send verification email.", e);
        }
    }

    /**
     * 이메일 인증 검증
     */
    @Transactional
    public ApiResponseTemplete<String> verifyEmail(String token) {
        Optional<EmailVerification> optionalVerification = emailVerificationRepository.findByToken(token);

        // 유효한 토큰인지 확인
        if (optionalVerification.isEmpty()) {
            return ApiResponseTemplete.<String>builder()
                    .status(400)
                    .success(false)
                    .message("유효하지 않거나 만료된 토큰입니다.")
                    .data(null)
                    .build();
        }

        EmailVerification verification = optionalVerification.get();
        User user = verification.getUser();

        // 이미 인증된 사용자인 경우
        if (user.getEmailVerified()) {
            return ApiResponseTemplete.<String>builder()
                    .status(200)
                    .success(true)
                    .message("이미 이메일 인증이 완료된 사용자입니다.")
                    .data(user.getEmail())
                    .build();
        }

        // 토큰 만료 여부 확인
        if (verification.getExpirationTime().before(new Date())) {
            emailVerificationRepository.delete(verification);
            return ApiResponseTemplete.<String>builder()
                    .status(400)
                    .success(false)
                    .message("인증 토큰이 만료되었습니다.")
                    .data(null)
                    .build();
        }

        // 이메일 인증 완료 처리
        user.setEmailVerified(true);
        userRepository.save(user);
        emailVerificationRepository.delete(verification);

        return ApiResponseTemplete.<String>builder()
                .status(200)
                .success(true)
                .message("이메일 인증이 성공적으로 완료되었습니다.")
                .data(user.getEmail())
                .build();
    }
}

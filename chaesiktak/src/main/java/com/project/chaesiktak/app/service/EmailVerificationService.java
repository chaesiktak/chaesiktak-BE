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
import org.springframework.security.crypto.password.PasswordEncoder;

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
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.mail.auth-code-expiration-millis}")
    private Long expirationMillis;

    @Autowired
    public EmailVerificationService(EmailVerificationRepository emailVerificationRepository,
                                    UserRepository userRepository,
                                    JavaMailSender mailSender,
                                    PasswordEncoder passwordEncoder) {
        this.emailVerificationRepository = emailVerificationRepository;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 이메일 인증 메일 발송 (기존 인증 데이터 삭제 후 새로운 데이터 저장)
     */
    @Transactional
    public ApiResponseTemplete<String> sendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일을 가진 사용자를 찾을 수 없습니다. : " + email));

        // 기존 이메일 인증 데이터를 삭제하여 하나의 이메일당 하나의 인증 데이터만 유지
        emailVerificationRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        LocalDateTime expirationTime = LocalDateTime.now().plus(Duration.ofMillis(expirationMillis));

        EmailVerification emailVerification = EmailVerification.builder()
                .user(user)
                .token(token)
                .expirationTime(Date.from(expirationTime.atZone(ZoneId.systemDefault()).toInstant()))
                .build();

        emailVerificationRepository.save(emailVerification);
        String verificationLink = "http://localhost:8080/api/verify/email?token=" + token;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject("채식탁 서비스 이메일 인증 절차 안내");
            helper.setText("<p>채식탁 서비스의 이메일 인증을 위해 하단의 링크를 클릭해주세요! :</p><a href=\"" + verificationLink + "\">인증하기</a>", true);

            mailSender.send(message);
            return ApiResponseTemplete.<String>builder()
                    .status(200)
                    .success(true)
                    .message("이메일 인증 링크가 발송되었습니다.")
                    .data(null)
                    .build();
        } catch (MessagingException e) {
            return ApiResponseTemplete.<String>builder()
                    .status(500)
                    .success(false)
                    .message("이메일 전송에 실패하였습니다.")
                    .data(null)
                    .build();
        }
    }

    /**
     * 이메일 인증 검증
     */
    @Transactional
    public ApiResponseTemplete<String> verifyEmail(String token) {
        Optional<EmailVerification> optionalVerification = emailVerificationRepository.findByToken(token);

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

        if (user.getEmailVerified()) {
            return ApiResponseTemplete.<String>builder()
                    .status(200)
                    .success(true)
                    .message("이미 이메일 인증이 완료된 사용자입니다.")
                    .data(user.getEmail())
                    .build();
        }

        if (verification.getExpirationTime().before(new Date())) {
            emailVerificationRepository.delete(verification);
            return ApiResponseTemplete.<String>builder()
                    .status(400)
                    .success(false)
                    .message("인증 토큰이 만료되었습니다.")
                    .data(null)
                    .build();
        }

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

    /**
     * 이메일 인증 재전송 (기존 인증 데이터 삭제 후 새로운 데이터 저장)
     */
    @Transactional
    public ApiResponseTemplete<String> resendVerificationEmail(String email, String password) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return ApiResponseTemplete.<String>builder()
                    .status(400)
                    .success(false)
                    .message("존재하지 않는 이메일입니다.")
                    .data(null)
                    .build();
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ApiResponseTemplete.<String>builder()
                    .status(400)
                    .success(false)
                    .message("아이디 또는 비밀번호를 확인해주세요.")
                    .data(null)
                    .build();
        }

        if (user.getEmailVerified()) {
            return ApiResponseTemplete.<String>builder()
                    .status(400)
                    .success(false)
                    .message("이미 이메일 인증이 완료되었습니다.")
                    .data(null)
                    .build();
        }

        return sendVerificationEmail(email);
    }
}

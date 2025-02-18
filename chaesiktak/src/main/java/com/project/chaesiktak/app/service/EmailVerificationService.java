package com.project.chaesiktak.app.service;

import com.project.chaesiktak.app.domain.EmailVerification;
import com.project.chaesiktak.app.domain.User;
import com.project.chaesiktak.app.repository.EmailVerificationRepository;
import com.project.chaesiktak.app.repository.UserRepository;
import com.project.chaesiktak.global.dto.ApiResponseTemplete;
import com.project.chaesiktak.global.exception.ErrorCode;
import com.project.chaesiktak.global.exception.model.CustomException;
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
        log.info("이메일 인증 요청: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("이메일 인증 실패: 존재하지 않는 이메일 ({})", email);
                    return new CustomException(ErrorCode.NOT_FOUND_USER_EXCEPTION,
                            ErrorCode.NOT_FOUND_USER_EXCEPTION.getMessage());
                });

        emailVerificationRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        LocalDateTime expirationTime = LocalDateTime.now().plus(Duration.ofMillis(expirationMillis));

        EmailVerification emailVerification = EmailVerification.builder()
                .user(user)
                .token(token)
                .expirationTime(Date.from(expirationTime.atZone(ZoneId.systemDefault()).toInstant()))
                .build();

        emailVerificationRepository.save(emailVerification);
        String verificationLink = "http://localhost:8080/api/verify/email?token=" + token; // 기존 방식으로 롤백

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject("채식탁 서비스 이메일 인증 절차 안내");
            helper.setText("<p>채식탁 서비스의 이메일 인증을 위해 하단의 링크를 클릭해주세요! :</p><a href=\"" + verificationLink + "\">인증하기</a>", true);

            mailSender.send(message);

            log.info("이메일 인증 링크 발송 완료: {}", email);

            return ApiResponseTemplete.<String>builder()
                    .status(200)
                    .success(true)
                    .message("이메일 인증 링크가 발송되었습니다.")
                    .data(null)
                    .build();
        } catch (MessagingException e) {
            log.error("이메일 전송 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.EMAIL_CERTIFICATION_SEND_MISSING_EXCEPTION, ErrorCode.EMAIL_CERTIFICATION_SEND_MISSING_EXCEPTION.getMessage());
        }
    }

    /**
     * 이메일 인증 검증
     */
    @Transactional
    public ApiResponseTemplete<String> verifyEmail(String token) {
        log.info("이메일 인증 검증 요청: token={}", token);

        EmailVerification verification = emailVerificationRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("이메일 인증 실패: 유효하지 않은 토큰 ({})", token);
                    return new CustomException(ErrorCode.EXPIRED_TOKEN_EXCEPTION, "유효하지 않거나 만료된 토큰입니다.");
                });

        User user = verification.getUser();

        if (user.getEmailVerified()) {
            log.info("이미 인증된 사용자: {}", user.getEmail());
            return ApiResponseTemplete.<String>builder()
                    .status(200)
                    .success(true)
                    .message("이미 이메일 인증이 완료된 사용자입니다.")
                    .data(user.getEmail())
                    .build();
        }

        if (verification.getExpirationTime().before(new Date())) {
            emailVerificationRepository.delete(verification);
            log.warn("이메일 인증 실패: 만료된 토큰 ({})", token);
            throw new CustomException(ErrorCode.EXPIRED_TOKEN_EXCEPTION, "인증 토큰이 만료되었습니다.");
        }

        user.setEmailVerified(true);
        userRepository.save(user);
        emailVerificationRepository.delete(verification);

        log.info("이메일 인증 성공: {}", user.getEmail());

        return ApiResponseTemplete.<String>builder()
                .status(200)
                .success(true)
                .message("이메일 인증이 성공적으로 완료되었습니다.")
                .data(user.getEmail())
                .build();
    }

    /**
     * 이메일 인증 재전송
     */
    @Transactional
    public ApiResponseTemplete<String> resendVerificationEmail(String email, String password) {
        log.info("이메일 인증 재전송 요청: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("이메일 인증 재전송 실패: 존재하지 않는 이메일 ({})", email);
                    return new CustomException(ErrorCode.NOT_FOUND_USER_EXCEPTION,
                            ErrorCode.NOT_FOUND_USER_EXCEPTION.getMessage());
                });

        if (user.getPassword() == null || !passwordEncoder.matches(password, user.getPassword())) {
            log.warn("이메일 인증 재전송 실패: 비밀번호 불일치 ({})", email);
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH_EXCEPTION, ErrorCode.PASSWORD_MISMATCH_EXCEPTION.getMessage());
        }

        if (user.getEmailVerified()) {
            log.info("이메일 인증 재전송 실패: 이미 인증된 사용자 ({})", email);
            throw new CustomException(ErrorCode.UNAUTHORIZED_EMAIL_EXCEPTION, ErrorCode.UNAUTHORIZED_EMAIL_EXCEPTION.getMessage());
        }

        return sendVerificationEmail(email);
    }
}

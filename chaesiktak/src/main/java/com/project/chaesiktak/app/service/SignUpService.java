package com.project.chaesiktak.app.service;

import com.project.chaesiktak.app.domain.RoleType;
import com.project.chaesiktak.app.domain.User;
import com.project.chaesiktak.app.domain.VeganType;
import com.project.chaesiktak.app.dto.user.UserSignUpDto;
import com.project.chaesiktak.app.repository.UserRepository;
import com.project.chaesiktak.global.dto.ApiResponseTemplete;
import com.project.chaesiktak.global.exception.ErrorCode;
import com.project.chaesiktak.global.exception.model.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SignUpService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService; // 이메일 인증 서비스 추가

    public ApiResponseTemplete<UserSignUpDto> signUp(UserSignUpDto userSignUpDto) throws Exception {

        if (userRepository.findByEmail(userSignUpDto.getEmail()).isPresent()) {
            throw new CustomException(ErrorCode.ALREADY_EXIT_EMAIL_EXCEPTION,
                    ErrorCode.ALREADY_EXIT_EMAIL_EXCEPTION.getMessage());
        }

        if (userRepository.findByEmail(userSignUpDto.getUserNickName()).isPresent()) {
            throw new CustomException(ErrorCode.ALREADY_EXIT_NICKNAME_EXCEPTION,
                    ErrorCode.ALREADY_EXIT_NICKNAME_EXCEPTION.getMessage());
        }

        User user = User.builder()
                .email(userSignUpDto.getEmail())
                .password(userSignUpDto.getPassword())
                .userName(userSignUpDto.getUserName())
                .userNickName(userSignUpDto.getUserNickName())
                .roleType(RoleType.USER) // 권한 설정:USER
                .veganType(VeganType.DEFAULT) // 기본 채식 상태:DEFAULT
                .emailVerified(false) // 이메일 인증 상태:false
                .build();

        user.passwordEncode(passwordEncoder);
        userRepository.save(user);

        // 이메일 인증 발송
        try {
            emailVerificationService.sendVerificationEmail(user.getEmail());
            // log.info("이메일 인증 전송 성공: {}", user.getEmail());
        } catch (Exception e) {
            // log.error("이메일 인증 전송에 실패함: {}", e.getMessage());
            throw new CustomException(ErrorCode.EMAIL_CERTIFICATION_SEND_MISSING_EXCEPTION,
                    ErrorCode.EMAIL_CERTIFICATION_SEND_MISSING_EXCEPTION.getMessage());
        }

        return ApiResponseTemplete.<UserSignUpDto>builder()
                .status(200)
                .success(true)
                .message("회원가입 성공! 이메일 인증을 완료해주세요.")
                .data(userSignUpDto)
                .build();
    }
}

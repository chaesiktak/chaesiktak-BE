package com.project.chaesiktak.app.service;

import com.project.chaesiktak.app.domain.RoleType;
import com.project.chaesiktak.app.domain.User;
import com.project.chaesiktak.app.domain.VeganType;
import com.project.chaesiktak.app.dto.user.UserSignUpDto;
import com.project.chaesiktak.app.repository.UserRepository;
import com.project.chaesiktak.global.dto.ApiResponseTemplete;
import com.project.chaesiktak.global.exception.ErrorCode;
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
    private final EmailVerificationService emailVerificationService;

    public ApiResponseTemplete<UserSignUpDto> signUp(UserSignUpDto userSignUpDto) {
        // 예외처리1. 이메일 중복 상태로 api 요청시
        if (userRepository.findByEmail(userSignUpDto.getEmail()).isPresent()) {
            return ApiResponseTemplete.<UserSignUpDto>builder()
                    .status(400)
                    .success(false)
                    .message(ErrorCode.ALREADY_EXIT_EMAIL_EXCEPTION.getMessage())
                    .data(null)
                    .build();
        }
        // 예외처리2. 닉네임 중복 상태로 api 요청시
        if (userRepository.findByUserNickName(userSignUpDto.getUserNickName()).isPresent()) {
            return ApiResponseTemplete.<UserSignUpDto>builder()
                    .status(400)
                    .success(false)
                    .message(ErrorCode.ALREADY_EXIT_NICKNAME_EXCEPTION.getMessage())
                    .data(null)
                    .build();
        }

        try {
            User user = User.builder()
                    .email(userSignUpDto.getEmail())
                    .password(userSignUpDto.getPassword())
                    .userName(userSignUpDto.getUserName())
                    .userNickName(userSignUpDto.getUserNickName())
                    .roleType(RoleType.ROLE_USER) // 권한 설정:USER
                    .veganType(VeganType.DEFAULT) // 기본 채식 상태:DEFAULT
                    .emailVerified(false) // 이메일 인증 상태:false
                    .build();

            user.passwordEncode(passwordEncoder);
            userRepository.save(user);

            // 이메일 인증 발송 처리
            emailVerificationService.sendVerificationEmail(user.getEmail());
            // 정상 응답 처리 : 회원가입 성공!
            return ApiResponseTemplete.<UserSignUpDto>builder()
                    .status(200)
                    .success(true)
                    .message("회원가입 성공! 이메일 인증을 완료해주세요.")
                    .data(userSignUpDto)
                    .build();
        } catch (Exception e) {
            // 이메일 인증 발송 실패시 응답 반환 처리
            return ApiResponseTemplete.<UserSignUpDto>builder()
                    .status(500)
                    .success(false)
                    .message(ErrorCode.EMAIL_CERTIFICATION_SEND_MISSING_EXCEPTION.getMessage())
                    .data(null)
                    .build();
        }
    }
}

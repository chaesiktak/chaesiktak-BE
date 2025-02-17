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
        // 예외처리 1: 필수 필드 누락 시 예외 처리
        if (userSignUpDto.getEmail() == null || userSignUpDto.getEmail().isBlank() ||
                userSignUpDto.getPassword() == null || userSignUpDto.getPassword().isBlank() ||
                userSignUpDto.getUserName() == null || userSignUpDto.getUserName().isBlank() ||
                userSignUpDto.getUserNickName() == null || userSignUpDto.getUserNickName().isBlank()) {

            return ApiResponseTemplete.<UserSignUpDto>builder()
                    .status(400)
                    .success(false)
                    .message("필수 입력 항목이 누락되었습니다. (이메일, 비밀번호, 이름, 닉네임)")
                    .data(null)
                    .build();
        }

        // 예외처리 2: 공백 포함 여부 체크 (이메일, 비밀번호, 닉네임, 사용자 이름)
        if (userSignUpDto.getEmail().matches(".*\\s.*") ||
                userSignUpDto.getPassword().matches(".*\\s.*") ||
                userSignUpDto.getUserName().matches(".*\\s.*") ||
                userSignUpDto.getUserNickName().matches(".*\\s.*")) {

            return ApiResponseTemplete.<UserSignUpDto>builder()
                    .status(400)
                    .success(false)
                    .message("입력값에 공백이 포함될 수 없습니다. (이메일, 비밀번호, 이름, 닉네임)")
                    .data(null)
                    .build();
        }

        // 예외처리 3: 이메일 중복 검사
        if (userRepository.findByEmail(userSignUpDto.getEmail()).isPresent()) {
            return ApiResponseTemplete.<UserSignUpDto>builder()
                    .status(400)
                    .success(false)
                    .message(ErrorCode.ALREADY_EXIT_EMAIL_EXCEPTION.getMessage())
                    .data(null)
                    .build();
        }

        // 예외처리 4: 닉네임 중복 검사
        if (userRepository.findByUserNickName(userSignUpDto.getUserNickName()).isPresent()) {
            return ApiResponseTemplete.<UserSignUpDto>builder()
                    .status(400)
                    .success(false)
                    .message(ErrorCode.ALREADY_EXIT_NICKNAME_EXCEPTION.getMessage())
                    .data(null)
                    .build();
        }

        try {
            // 불필요한 공백 제거
            String trimmedEmail = userSignUpDto.getEmail().trim();
            String trimmedPassword = userSignUpDto.getPassword().trim();
            String trimmedUserName = userSignUpDto.getUserName().trim();
            String trimmedUserNickName = userSignUpDto.getUserNickName().trim();

            // 사용자 객체 생성 및 저장
            User user = User.builder()
                    .email(trimmedEmail)
                    .password(trimmedPassword)
                    .userName(trimmedUserName)
                    .userNickName(trimmedUserNickName)
                    .roleType(RoleType.USER) // 기본 권한 설정: USER
                    .veganType(VeganType.DEFAULT) // 기본 채식 상태: DEFAULT
                    .emailVerified(false) // 이메일 인증 상태: false
                    .build();

            user.passwordEncode(passwordEncoder); // 비밀번호 암호화
            userRepository.save(user);

            // 이메일 인증 발송
            emailVerificationService.sendVerificationEmail(user.getEmail());

            // 정상 응답 처리 : 회원가입 성공!
            return ApiResponseTemplete.<UserSignUpDto>builder()
                    .status(200)
                    .success(true)
                    .message("회원가입 성공! 이메일 인증을 완료해주세요.")
                    .data(userSignUpDto)
                    .build();
        } catch (Exception e) {
            // 이메일 인증 발송 실패 시 예외 처리
            return ApiResponseTemplete.<UserSignUpDto>builder()
                    .status(500)
                    .success(false)
                    .message(ErrorCode.EMAIL_CERTIFICATION_SEND_MISSING_EXCEPTION.getMessage())
                    .data(null)
                    .build();
        }
    }
}

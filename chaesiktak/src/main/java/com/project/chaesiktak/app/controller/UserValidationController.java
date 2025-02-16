package com.project.chaesiktak.app.controller;

import com.project.chaesiktak.app.repository.UserRepository;
import com.project.chaesiktak.global.dto.ApiResponseTemplete;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/check")
@RequiredArgsConstructor
public class UserValidationController {

    private final UserRepository userRepository;
    // 이메일 형식 검증 정규식 (영문, 숫자, 특수문자(+_.-)@영문, 숫자, 특수문자(-).영문(2~6자리))
    private static final Pattern EMAIL_PATTERN = Pattern.compile( "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$" );
    // 닉네임 정규식 (한글, 영어, 숫자만 허용)
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9]+$");
    /**
     * 이메일 중복 확인
     */
    @PostMapping("/email")
    public ResponseEntity<ApiResponseTemplete<Boolean>> checkEmail(@RequestBody Map<String, Object> request) {
        // 예외처리1. 잘못된 요청 형식
        if (!request.containsKey("email") || !(request.get("email") instanceof String email) || email.isBlank()) {
            return ResponseEntity.badRequest().body(
                    ApiResponseTemplete.<Boolean>builder()
                            .status(400)
                            .success(false)
                            .message("잘못된 요청 형식입니다. email 필드를 사용해야 하며 문자열이어야 합니다.")
                            .data(null)
                            .build()
            );
        }
        // 예외처리2. 잘못된 이메일 형식
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ResponseEntity.badRequest().body(
                    ApiResponseTemplete.<Boolean>builder()
                            .status(400)
                            .success(false)
                            .message("올바른 이메일 주소를 입력해주세요.")
                            .data(null)
                            .build()
            );
        }

        boolean isDuplicate = userRepository.findByEmail(email).isPresent();
        // 정상응답처리 : 중복 또는 가능
        return ResponseEntity.ok(ApiResponseTemplete.<Boolean>builder()
                .status(200)
                .success(true)
                .message(isDuplicate ? "이미 사용 중인 이메일입니다." : "사용 가능한 이메일입니다.")
                .data(isDuplicate) // true(중복), false(사용 가능)
                .build());
    }
    /**
     * 닉네임 중복 확인 API
     */
    @PostMapping("/nickname")
    public ResponseEntity<ApiResponseTemplete<Boolean>> checkNickname(@RequestBody Map<String, Object> request) {
        // 예외처리1. 잘못된 요청 형식
        if (!request.containsKey("userNickName") || !(request.get("userNickName") instanceof String nickname) || nickname.isBlank()) {
            return ResponseEntity.badRequest().body(
                    ApiResponseTemplete.<Boolean>builder()
                            .status(400)
                            .success(false)
                            .message("잘못된 요청 형식입니다. userNickName 필드를 사용해야 하며 문자열이어야 합니다.")
                            .data(null)
                            .build()
            );
        }
        // 예외처리2. 잘못된 닉네임 형식
        if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
            return ResponseEntity.badRequest().body(
                    ApiResponseTemplete.<Boolean>builder()
                            .status(400)
                            .success(false)
                            .message("닉네임은 한글, 영어, 숫자만 입력 가능합니다.")
                            .data(null)
                            .build()
            );
        }
        boolean isDuplicate = userRepository.findByUserNickName(nickname).isPresent();
        // 정상응답처리 : 중복 또는 가능
        return ResponseEntity.ok(ApiResponseTemplete.<Boolean>builder()
                .status(200)
                .success(true)
                .message(isDuplicate ? "이미 사용 중인 닉네임입니다." : "사용 가능한 닉네임입니다.")
                .data(isDuplicate) // true(중복), false(사용 가능)
                .build());
    }
}

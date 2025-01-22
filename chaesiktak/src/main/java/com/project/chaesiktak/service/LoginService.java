package com.project.chaesiktak.service;


import com.project.chaesiktak.dto.LoginDTO;
import com.project.chaesiktak.entity.UserEntity;
import com.project.chaesiktak.jwt.JWTUtil;
import com.project.chaesiktak.repository.UserRepository;
import com.project.chaesiktak.dto.CustomUserDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    public LoginService(AuthenticationManager authenticationManager, UserRepository userRepository, JWTUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public String login(LoginDTO loginDTO) throws IllegalStateException {
        // 사용자 인증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
        );

        // 인증 성공 시 사용자 정보 가져오기
        CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();

        // 이메일 인증 상태 확인
        UserEntity userEntity = userRepository.findByUsername(authenticatedUser.getUsername());
        if (!userEntity.getEmailVerified()) {  // 이메일 인증 여부 체크
            throw new IllegalStateException("이메일 인증이 필요합니다.");
        }

        // JWT 토큰 생성 및 반환 (category, username, role, 만료 시간)
        String category = "user"; // 예시: 카테고리 설정
        String username = authenticatedUser.getUsername();
        String role = authenticatedUser.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .orElse("ROLE_USER");
        long expirationTime = 3600000L; // 1시간 만료 (밀리초)

        return jwtUtil.createJwt(category, username, role, expirationTime);
    }
}

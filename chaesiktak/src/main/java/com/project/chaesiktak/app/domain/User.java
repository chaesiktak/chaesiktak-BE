package com.project.chaesiktak.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "USERS")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Long id;

    @Column(name = "USER_EMAIL", nullable = false, unique = true)
    private String email;

    private String password;

    @Setter
    private String userName;

    @Setter
    @Column(nullable = false, unique = true)
    private String userNickName;

    private String refreshToken;

    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    @Setter
    @Enumerated(EnumType.STRING)
    private VeganType veganType;

    @Column(nullable = false)
    private boolean emailVerified = false;

    // 이메일 인증 여부 반환
    public Boolean getEmailVerified() { return emailVerified; }
    // 이메일 인증 여부 설정
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
    // 유저 권한 정보 반환
    public void authorizeUser() { this.roleType = RoleType.USER; }
    // 비밀번호 암호화 후 반환
    public void passwordEncode(PasswordEncoder passwordEncoder) { this.password = passwordEncoder.encode(this.password); }
    // 비밀번호 암호화 후 재설정
    public void setEncodedPassword(PasswordEncoder passwordEncoder, String newPassword) { this.password = passwordEncoder.encode(newPassword); }
    // 리프레쉬 토큰 반환
    public void updateRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    @Builder
    public User(Long id,
                String email, String password,
                String userName, String userNickName,
                String refreshToken, Boolean emailVerified,
                RoleType roleType, VeganType veganType) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.userName = userName;
        this.userNickName = userNickName;
        this.refreshToken = refreshToken;
        this.emailVerified = emailVerified;
        this.roleType = roleType;
        this.veganType = veganType;
    }
}

package com.project.chaesiktak.global.security;

import com.project.chaesiktak.app.repository.UserRepository;
import com.project.chaesiktak.global.exception.ErrorCode;
import com.project.chaesiktak.global.exception.model.CustomException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

@Component
@Getter
@Slf4j
public class TokenService {

    private final Key key;
    private final long accessTokenValidityTime;
    private final long refreshTokenValidityTime;
    private final UserRepository userRepository;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    private static final String BEARER = "Bearer ";
    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String EMAIL_CLAIM = "email";

    /**
     * 생성자: JWT 키 및 만료 시간 설정
     */
    public TokenService(UserRepository userRepository,
                        @Value("${jwt.access.expiration}") long accessTokenValidityTime,
                        @Value("${jwt.refresh.expiration}") long refreshTokenValidityTime,
                        @Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityTime = accessTokenValidityTime;
        this.refreshTokenValidityTime = refreshTokenValidityTime;
        this.userRepository = userRepository;
    }

    /**
     * Access Token 생성
     */
    public String createAccessToken(String email) {
        Date expirationTime = new Date(System.currentTimeMillis() + accessTokenValidityTime);

        return Jwts.builder()
                .subject(ACCESS_TOKEN_SUBJECT)
                .claim(EMAIL_CLAIM, email)
                .issuedAt(new Date())
                .expiration(expirationTime)
                .signWith(key)
                .compact();
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken() {
        Date expirationTime = new Date(System.currentTimeMillis() + refreshTokenValidityTime);

        return Jwts.builder()
                .subject(REFRESH_TOKEN_SUBJECT)
                .issuedAt(new Date())
                .expiration(expirationTime)
                .signWith(key)
                .compact();
    }

    /**
     * 토큰 검증 (유효성 및 만료 체크)
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("[토큰 만료] {}", e.getMessage());
            throw new CustomException(ErrorCode.EXPIRED_TOKEN_EXCEPTION, "토큰이 만료되었습니다.");
        } catch (JwtException | IllegalArgumentException e) {
            log.error("[토큰 검증 실패] {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_TOKEN_EXCEPTION, "유효하지 않은 토큰입니다.");
        }
    }

    /**
     * 토큰에서 사용자 이메일 추출
     */
    public Optional<String> extractEmail(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return Optional.ofNullable(claims.get(EMAIL_CLAIM, String.class));
        } catch (ExpiredJwtException e) {
            log.error("[토큰 만료] 이메일 추출 불가: {}", e.getMessage());
            throw new CustomException(ErrorCode.EXPIRED_TOKEN_EXCEPTION, "토큰이 만료되었습니다.");
        } catch (JwtException e) {
            log.error("[토큰 검증 실패] 이메일 추출 불가: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_TOKEN_EXCEPTION, "유효하지 않은 토큰입니다.");
        }
    }

    /**
     * Refresh Token을 DB에 저장
     */
    @Transactional
    public void updateRefreshToken(String email, String refreshToken) {
        userRepository.findByEmail(email)
                .ifPresentOrElse(
                        user -> {
                            user.updateRefreshToken(refreshToken);
                            log.info("리프레시 토큰 저장 완료 (이메일: {})", email);
                        },
                        () -> {
                            log.error("[토큰 업데이트 실패] 유저를 찾을 수 없음 (이메일: {})", email);
                            throw new CustomException(ErrorCode.NOT_FOUND_USER_EXCEPTION,
                                    ErrorCode.NOT_FOUND_USER_EXCEPTION.getMessage());
                        }
                );
    }

    /**
     * HTTP 요청에서 AccessToken 추출
     */
    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessHeader))
                .filter(accessToken -> accessToken.startsWith(BEARER))
                .map(accessToken -> accessToken.replace(BEARER, ""));
    }

    /**
     * HTTP 요청에서 RefreshToken 추출
     */
    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(refreshHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }

    /**
     * AccessToken을 HTTP 응답 헤더에 추가
     */
    @Transactional
    public void sendAccessToken(HttpServletResponse response, String accessToken) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader(accessHeader, BEARER + accessToken);
        log.info("[AccessToken 발급] {}", accessToken);
    }

    /**
     * AccessToken & RefreshToken을 HTTP 응답 헤더에 추가
     */
    @Transactional
    public void sendAccessAndRefreshToken(HttpServletResponse response,
                                          String accessToken, String refreshToken) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader(accessHeader, BEARER + accessToken);
        response.setHeader(refreshHeader, BEARER + refreshToken);
        log.info("[AccessToken & RefreshToken 발급 완료]");
    }
}

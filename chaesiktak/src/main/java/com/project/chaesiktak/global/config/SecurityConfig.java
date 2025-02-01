package com.project.chaesiktak.global.config;

import com.project.chaesiktak.app.domain.User;
import com.project.chaesiktak.app.dto.user.CustomUserDetails;
import com.project.chaesiktak.app.service.LoginService;
import com.project.chaesiktak.global.security.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import java.io.IOException;
import java.util.List;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.naming.AuthenticationException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginService loginService;
    private final TokenService tokenService;  // TokenService 주입

    /**
     * 비밀번호 암호화 설정.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * AuthenticationManager 설정
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(loginService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(List.of(authProvider));
    }

    /**
     * SecurityFilterChain 설정 (JWT 토큰 필터 포함)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // CSRF 비활성화 (POST 요청 가능하게)
                .formLogin(AbstractHttpConfigurer::disable) // form 기반 로그인 비활성화 (/api/login 경로를 사용하므로)
                .httpBasic(AbstractHttpConfigurer::disable) //
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/sign-up").permitAll() // 회원가입 요청 허용
                        .requestMatchers("/api/check/email").permitAll() // 이메일 중복 확인 요청 허용
                        .requestMatchers("/api/check/nickname").permitAll() // 닉네임 중복 확인 요청 허용
                        .requestMatchers("/api/login").permitAll() // 로그인 요청 허용
                        .requestMatchers("/api/verify/email").permitAll() // 이메일 인증 API 허용
                        .requestMatchers("/swagger-ui.html", "/v3/api-docs/**", "/swagger-ui/**").permitAll() // Swagger 경로 허용
                        .anyRequest().authenticated() // 나머진 인증 필요
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("Unauthorized: " + authException.getMessage());
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write("Forbidden: " + accessDeniedException.getMessage());
                        })
                )
                .addFilterBefore(new OncePerRequestFilter() { // JWT 필터 추가
                    @Override
                    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                            throws ServletException, IOException {

                        // Authorization 헤더에서 토큰 추출
                        String token = getTokenFromHeader(request);

                        if (token != null) {
                            try {
                                // TokenService로 토큰 검증
                                tokenService.validateToken(token);

                                // 토큰에서 사용자 정보(email 등) 추출
                                String email = tokenService.extractEmail(token).orElseThrow(() -> new AuthenticationException("Token does not contain email"));


                                // UserDetails를 CustomUserDetails로 캐스팅하여 User 객체를 가져옴
                                // CustomUserDetails customUserDetails = (CustomUserDetails) loginService.loadUserByUsername(email);
                                // User user = customUserDetails.getUser();  // User 객체 가져오기
                                CustomUserDetails customUserDetails = (CustomUserDetails) loginService.loadUserByUsername(email);

                                // Spring Security 인증 설정
                                Authentication authToken = new UsernamePasswordAuthenticationToken(
                                        customUserDetails, null, customUserDetails.getAuthorities()
                                );
                                SecurityContextHolder.getContext().setAuthentication(authToken);
                            } catch (Exception e) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.getWriter().write("{\"error\":\"Invalid or expired token\"}");
                                e.printStackTrace();
                                return;
                            }
                        }

                        filterChain.doFilter(request, response); // 계속해서 필터 체인 실행
                    }
                }, UsernamePasswordAuthenticationFilter.class);  // UsernamePasswordAuthenticationFilter 앞에 필터 추가

        return http.build();
    }

    /**
     * Authorization 헤더에서 토큰을 추출
     */
    private String getTokenFromHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);  // "Bearer " 이후의 토큰 부분 반환
        }
        return null;  // 토큰이 없는 경우
    }
}

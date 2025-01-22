package com.project.chaesiktak.jwt;

import com.project.chaesiktak.dto.CustomUserDetails;
import com.project.chaesiktak.entity.UserEntity;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {

        this.jwtUtil = jwtUtil;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 헤더에서 access 키에 담긴 토큰 추출
        String accessToken = request.getHeader("access");

        // 토큰이 없는 경우, 다음 필터로 넘어감
        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 토큰 만료 여부 확인
            jwtUtil.isExpired(accessToken);

            // 토큰의 카테고리가 "access"인지 확인
            String category = jwtUtil.getCategory(accessToken);
            if (!"access".equals(category)) {
                // 유효하지 않은 액세스 토큰
                sendErrorResponse(response, "Invalid access token", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // username, role 값을 추출하여 인증 객체 생성
            String username = jwtUtil.getUsername(accessToken);
            String role = jwtUtil.getRole(accessToken);

            // 사용자 엔티티로 CustomUserDetails 생성
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername(username);
            userEntity.setRole(role);

            CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);

            // Spring Security 인증 설정
            Authentication authToken = new UsernamePasswordAuthenticationToken(
                    customUserDetails, null, customUserDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (ExpiredJwtException e) {
            // 액세스 토큰이 만료된 경우 처리
            sendErrorResponse(response, "Access token expired", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } catch (Exception e) {
            // 기타 오류 처리
            sendErrorResponse(response, "Invalid token processing", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }


        // 정상 처리된 경우, 다음 필터로 이동
        filterChain.doFilter(request, response);
    }

    /**
     * 클라이언트에 오류 응답을 보내는 헬퍼 메서드
     */
    private void sendErrorResponse(HttpServletResponse response, String message, int statusCode) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);

        PrintWriter writer = response.getWriter();
        writer.write("{\"error\":\"" + message + "\"}");
        writer.flush();
        writer.close();
    }
}

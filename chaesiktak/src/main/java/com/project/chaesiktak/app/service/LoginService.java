package com.project.chaesiktak.app.service;

import com.project.chaesiktak.app.domain.User;
import com.project.chaesiktak.app.dto.user.CustomUserDetails;
import com.project.chaesiktak.app.repository.UserRepository;
import com.project.chaesiktak.global.exception.ErrorCode;
import com.project.chaesiktak.global.exception.model.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.NOT_FOUND_USER_EXCEPTION,
                        ErrorCode.NOT_FOUND_USER_EXCEPTION.getMessage()
                ));

        return new CustomUserDetails(user);
    }

    /**
     * 이메일 인증 여부 확인
     */
    public boolean isEmailVerified(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.NOT_FOUND_USER_EXCEPTION,
                        ErrorCode.NOT_FOUND_USER_EXCEPTION.getMessage()
                ));

        return user.getEmailVerified();
    }
}

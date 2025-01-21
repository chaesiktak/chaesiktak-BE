package com.project.chaesiktak.service;

import com.project.chaesiktak.dto.CustomUserDetails;
import com.project.chaesiktak.entity.UserEntity;
import com.project.chaesiktak.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userData = userRepository.findByUsername(username);

        if (userData == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
        }

        if (!userData.getEmailVerified()) {
            throw new IllegalStateException("이메일 인증이 완료되지 않았습니다.");
        }

        return new CustomUserDetails(userData);
    }

}

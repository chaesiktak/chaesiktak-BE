package com.project.chaesiktak.app.repository;

import com.project.chaesiktak.app.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
        /**
         * 이메일 값을 기준으로 User 엔티티 조회.
         */
        Optional<User> findByEmail(String email);
        /**
         * 사용자 닉네임을 기준으로 User 엔티티 조회.
         */
        Optional<User> findByUserNickName(String userNickName);
        /**
         * 사용자 Rtoken 기준으로 User 엔티티 조회.
         */
        Optional<User> findByRefreshToken(String refreshToken);
}

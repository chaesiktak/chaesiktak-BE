package com.project.chaesiktak.repository;

import com.project.chaesiktak.entity.EmailVerificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerificationEntity, Long> {

    /**
     * 특정 토큰으로 EmailVerificationEntity를 조회.
     */
    Optional<EmailVerificationEntity> findByToken(String token);

    /**
     * 특정 username으로 EmailVerificationEntity를 조회.
     */
    Optional<EmailVerificationEntity> findByUsername(String username);

    /**
     * 특정 username의 인증 데이터를 삭제 (Transactional 필수).
     */
    @Transactional
    void deleteByUsername(String username);
}


package com.project.chaesiktak.app.repository;

import com.project.chaesiktak.app.entity.WithdrawalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalRepository extends JpaRepository<WithdrawalEntity, Long> {
}

package com.project.chaesiktak.app.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "WITHDRAWALS")
public class WithdrawalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime withdrawalDate;

    @Builder
    public WithdrawalEntity(String email, String reason) {
        this.email = email;
        this.reason = reason;
        this.withdrawalDate = LocalDateTime.now();
    }
}

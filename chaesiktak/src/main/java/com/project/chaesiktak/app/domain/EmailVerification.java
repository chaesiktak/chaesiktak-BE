package com.project.chaesiktak.app.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.Date;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "USER_EMAIL", referencedColumnName = "USER_EMAIL")
    private User user;

    private String token;
    private Date expirationTime;

    @Builder
    public EmailVerification(User user, String token, Date expirationTime) {
        this.user = user;
        this.token = token;
        this.expirationTime = expirationTime;
    }
}

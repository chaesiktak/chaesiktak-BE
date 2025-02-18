package com.project.chaesiktak.app.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPasswordDto {
    private String email;
    private String currentPassword;
    private String newPassword;

    // 임시 비밀번호 Dto
    @Getter
    @Setter
    public static class UserPasswordResetDto {
        private String email;
        private String userName;
    }
}

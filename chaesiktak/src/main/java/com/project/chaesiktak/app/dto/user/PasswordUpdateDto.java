package com.project.chaesiktak.app.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordUpdateDto {
    private String email;
    private String currentPassword;
    private String newPassword;
}

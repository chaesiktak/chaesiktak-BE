package com.project.chaesiktak.app.dto.user;

import com.project.chaesiktak.app.domain.VeganType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateVeganDto {
    private VeganType veganType;
}

package com.project.chaesiktak.app.dto.user;
import com.project.chaesiktak.app.domain.VeganType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserMypageDto {
    private String email;
    private String userNickName;
    private String userName;
    private VeganType veganType;
}

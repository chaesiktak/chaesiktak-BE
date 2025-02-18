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

    //유저 이메일
    @Getter
    @Setter
    public static class UserEmailDto {
        private String email;
    }

    // 유저 이름
    @Getter
    @Setter
    public static class UserNameDto {
        private String userName;
    }

    // 유저 닉네임
    @Getter
    @Setter
    public static class UserNicknameDto {
        private String userNickName;
    }

    // 유저 채식 상태
    @Getter
    @Setter
    public static class UserVeganDto {
        private VeganType veganType;
    }
}

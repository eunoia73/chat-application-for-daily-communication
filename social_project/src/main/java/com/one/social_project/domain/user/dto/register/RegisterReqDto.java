package com.one.social_project.domain.user.dto.register;

import com.one.social_project.domain.user.dto.OAuth2UserInfo;
import lombok.Data;

@Data
public class RegisterReqDto {
    private String email;
    private String password;
    private String confirmPassword;
    private String nickname;

    private OAuth2UserInfo oauth2UserInfo;
}

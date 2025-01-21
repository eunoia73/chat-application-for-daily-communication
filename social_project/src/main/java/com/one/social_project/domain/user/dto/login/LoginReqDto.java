package com.one.social_project.domain.user.dto.login;

import lombok.Data;

@Data
public class LoginReqDto {
    private String email;
    private String password;
    private String accessToken;
    private Boolean isSocialLogin = false;
}

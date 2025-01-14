package com.one.social_project.domain.user.basic.dto;

import lombok.Data;

@Data
public class UserRegisterDto {
    private String email;
    private String password;
    private String nickname;
    private boolean marketingAgreed;
}

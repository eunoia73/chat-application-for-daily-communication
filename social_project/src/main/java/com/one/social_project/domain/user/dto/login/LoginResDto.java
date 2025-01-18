package com.one.social_project.domain.user.dto.login;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class LoginResDto {
    private String email;
    private String accessToken;
    private String refreshToken;
}

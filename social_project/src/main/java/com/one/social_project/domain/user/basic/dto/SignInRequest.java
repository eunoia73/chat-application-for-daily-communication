package com.one.social_project.domain.user.basic.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SignInRequest {
    private String email;
    private String password;
}

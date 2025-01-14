package com.one.social_project.domain.user.basic.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserLogoutRequest {
    private String token;
}

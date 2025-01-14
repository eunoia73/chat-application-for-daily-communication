package com.one.social_project.domain.user.basic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserRegisterResultDto {

    private boolean isSuccess;
    private String errorCode;
}

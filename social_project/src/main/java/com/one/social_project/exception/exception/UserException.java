package com.one.social_project.exception.exception;

import com.one.social_project.exception.errorCode.UserErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserException extends RuntimeException {
    private final UserErrorCode userErrorCode;
}

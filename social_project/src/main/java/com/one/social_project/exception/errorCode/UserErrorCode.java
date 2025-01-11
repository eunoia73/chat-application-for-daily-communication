package com.one.social_project.exception.errorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import com.one.social_project.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    // 회원가입
    EMAIL_ALREADY_IN_USE(HttpStatus.BAD_REQUEST, "이미 사용 중인 사용자 이메일입니다."),
    EMAIL_RESIGN_IN_USE(HttpStatus.BAD_REQUEST, "탈퇴한 사용자 이메일입니다."),
    EMAIL_VERIFICATION_NOT_COMPLETE(HttpStatus.UNAUTHORIZED, "이메일 인증이 완료되지 않았습니다."),
    NICKNAME_ALREADY_IN_USE(HttpStatus.CONFLICT, "이미 사용 중인 사용자 닉네임입니다."),

    // 인증
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    PASSWORD_INVALID(HttpStatus.UNAUTHORIZED, "비밀번호 오류"),
    // 인가
    ACCESS_DENIED(HttpStatus.UNAUTHORIZED, "권한이 없습니다"),
    LOGOUT_USER(HttpStatus.UNAUTHORIZED,"로그아웃된 사용자입니다");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return this.status;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

}

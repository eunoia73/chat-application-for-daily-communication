package com.one.social_project.exception.handler;

import com.one.social_project.exception.ErrorCode;
import com.one.social_project.exception.errorCode.UserErrorCode;
import com.one.social_project.exception.exception.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;
import com.one.social_project.exception.dto.ErrorResponse;

@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorResponse> handleUserException(UserException ex)
    {
        UserErrorCode errorCode = ex.getUserErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(makeErrorResponse(errorCode.getHttpStatus(), errorCode.getMessage()));

    }



    // 예외처리에 관한 http를 보내는 코드
    private ResponseEntity<Object> handleExceptionInternal(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(makeErrorResponse(errorCode.getHttpStatus(), errorCode.getMessage()));
    }

    private ErrorResponse makeErrorResponse(HttpStatus status, String message) {
        return new ErrorResponse(status,message);
    }

}

package com.one.social_project.domain.file.error;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // 400 Bad Request 반환
public class UnsupportedFileFormatException extends RuntimeException {
    public UnsupportedFileFormatException(String message) {
        super(message);
    }
}

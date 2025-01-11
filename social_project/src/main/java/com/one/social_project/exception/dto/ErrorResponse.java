package com.one.social_project.exception.dto;


import lombok.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@Builder
@AllArgsConstructor
@Data
@Setter
public class ErrorResponse {
    private final HttpStatus status;
    private final String message;

}
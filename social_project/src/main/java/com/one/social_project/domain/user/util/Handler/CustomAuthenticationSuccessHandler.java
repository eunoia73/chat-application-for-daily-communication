package com.one.social_project.domain.user.util.Handler;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private StringRedisTemplate redisTemplate;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // 사용자 정보 가져오기 (예: 이메일)
        String username = authentication.getName(); // username이나 이메일

        System.out.println(username+" 추가");
        // Redis에 사용자 정보 저장
        redisTemplate.opsForValue().set(username, "logged_in"); // 1시간 동안 로그인 상태 유지

        response.sendRedirect("/home"); // 로그인 후 이동할 페이지
    }
}
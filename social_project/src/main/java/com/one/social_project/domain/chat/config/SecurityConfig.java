package com.one.social_project.domain.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/chat/**","/chat/**").permitAll() // /api/chat/**는 인증 없이 접근 가능
                        .anyRequest().authenticated() // 나머지 요청은 인증 필요
                );

        return http.build();
    }
}

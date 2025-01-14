package com.one.social_project.domain.user.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;  // RedisTemplate의 타입 명시

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String enteredEmail = authentication.getName();
        String enteredPassword = authentication.getCredentials().toString();

        // 사용자 정보 로드
        UserDetails userDetails = userDetailsService.loadUserByUsername(enteredEmail);

        // 비밀번호 검증
        if (passwordEncoder.matches(enteredPassword, userDetails.getPassword())) {
            // 로그인 성공 후 Redis에 사용자 정보 저장
            redisTemplate.opsForSet().add("login", enteredEmail);

            System.out.println(redisTemplate.opsForValue().get(enteredEmail));
            // 인증된 사용자 반환 (비밀번호는 포함하지 않음)
            return new UsernamePasswordAuthenticationToken(
                    enteredEmail, null, userDetails.getAuthorities()
            );
        } else {
            throw new BadCredentialsException("Invalid Password!");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
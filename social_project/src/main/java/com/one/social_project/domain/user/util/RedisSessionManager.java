package com.one.social_project.domain.user.util;

import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSessionManager {

    private final StringRedisTemplate redisTemplate;

    public boolean checkLogin(String refreshToken)
    {
        try {
            return redisTemplate.opsForSet().isMember("login", refreshToken) != null;
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 연결 실패: {}", e.getMessage());
            return false;
        }
    }

    // JWT 토큰을 블랙리스트에 추가하는 메소드
    public void addToBlacklist(String token) {
        try {
            redisTemplate.opsForValue().set("blacklist:" + token, "true", 24, java.util.concurrent.TimeUnit.HOURS);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 연결 실패: {}", e.getMessage());
        }
    }

    // JWT 토큰이 블랙리스트에 있는지 확인하는 메소드
    public boolean isTokenBlacklisted(String token) {
        try {
            Boolean hasKey = redisTemplate.hasKey("blacklist:" + token);
            return hasKey != null && hasKey;
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 연결 실패: {}", e.getMessage());
            return false;
        }
    }
}
package com.one.social_project.domain.user.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisSessionManager {

    private final StringRedisTemplate redisTemplate;

    public boolean checkLogin(String refreshToken)
    {
        return redisTemplate.opsForSet().isMember("login", refreshToken) != null;
    }

    // JWT 토큰을 블랙리스트에 추가하는 메소드
    public void addToBlacklist(String token) {
        // JWT 토큰을 Redis에 저장하고, 일정 시간이 지나면 자동으로 삭제되도록 TTL 설정 (예: 24시간)
        redisTemplate.opsForValue().set("blacklist:" + token, "true", 24, java.util.concurrent.TimeUnit.HOURS);
    }

    // JWT 토큰이 블랙리스트에 있는지 확인하는 메소드
    public boolean isTokenBlacklisted(String token) {
        return redisTemplate.hasKey("blacklist:" + token);
    }
}
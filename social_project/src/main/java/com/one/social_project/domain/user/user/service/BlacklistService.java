package com.one.social_project.domain.user.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.one.social_project.domain.user.util.RedisSessionManager;

@Service
@lombok.RequiredArgsConstructor
public class BlacklistService {

    private final RedisSessionManager redisSessionManager;

    // JWT 토큰을 블랙리스트에 추가하는 메소드
    public void addToBlacklist(String token) {
        // JWT 토큰을 Redis에 저장하고, 일정 시간이 지나면 자동으로 삭제되도록 TTL 설정 (예: 24시간)
        redisSessionManager.addToBlacklist(token);
    }

    // JWT 토큰이 블랙리스트에 있는지 확인하는 메소드
    public boolean isTokenBlacklisted(String token) {
        return redisSessionManager.isTokenBlacklisted(token);
    }
}

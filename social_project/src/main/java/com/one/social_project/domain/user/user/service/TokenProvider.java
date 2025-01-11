package com.one.social_project.domain.user.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.one.social_project.domain.user.user.entity.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import com.one.social_project.domain.user.user.repository.*;
import com.one.social_project.domain.user.ApplicationConstants;

import static com.one.social_project.domain.user.ApplicationConstants.JWT_SECRET_KEY;

@Service
@RequiredArgsConstructor
public class TokenProvider {

    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final UserRepository userRepository;
    private final Environment env;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RedisTemplate<String, String> redisTemplate;

    private SecretKey secretKey = null;

    // Bean 으로 등록이 된 이후에 실행되도록 하여, secret Key 를 초기화 시킨다.
    @PostConstruct
    public void initializeTokenProvider() {
        String secret = env.getProperty(JWT_SECRET_KEY, ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }


    // Access Token 을 생성하는 메소드
    public String createAccessToken(Long userId, String username, String authorities) {

        Date now = new Date();

        // JWT의 만료 시간 설정 (현재 시간 + ACCESS_TOKEN_EXPIRATION)
        Date expirationDate = new Date(now.getTime() + ApplicationConstants.ACCESS_TOKEN_EXPIRATION);

        // JWT 토큰 생성
        return Jwts.builder()
                .claim("userId", userId)  // 사용자 ID를 클레임에 추가
                .claim("email", username)  // 사용자 이름을 클레임에 추가
                .claim("authorities", authorities)  // 사용자 권한을 클레임에 추가
                .setIssuedAt(now)  // 토큰 발급 시간
                .setExpiration(expirationDate)  // 토큰 만료 시간
                .signWith(secretKey)  // 서명에 사용할 비밀 키
                .compact();  // 토큰 생성
    }

    // Refresh Token 을 발급하는 메소드
    public String createRefreshToken() {

        return Jwts.builder()
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + ApplicationConstants.REFRESH_TOKEN_EXPIRATION))
                .signWith(secretKey)
                .compact();
    }

    public String getEmailFromToken(String token) throws JsonProcessingException {
        String email = getPayloadFromJWTToken(token).get("email").toString();
        return email;
    }

    // 기존에 클라이언트가 보낸 만료된 oldAccessToken 을 이용하여 새 access Token 을 발급하는 과정이다.
    @Transactional
    public String recreateAccessToken(String oldAccessToken) throws JsonProcessingException {
        String username = getPayloadFromJWTToken(oldAccessToken).get("username").toString();
        String authorities = getPayloadFromJWTToken(oldAccessToken).get("authorities").toString();

        Users user = userRepository.findByEmail(username).orElseThrow(() -> new IllegalStateException("해당하는 유저정보가 존재하지 않습니다"));

        // 현재 클라이언트가 보낸 Refresh Token 의 주인이 맞는 지 검증 + 재발급 횟수가 남아있는 지 검증
        UserRefreshToken userRefreshToken = userRefreshTokenRepository
                .findByUserAndReIssueCountLessThan(user, ApplicationConstants.REFRESH_TOKEN_RE_ISSUE_LIMIT)
                .orElseThrow(() -> new ExpiredJwtException(null, null, "Refresh Token Expired!"));

        // Access Token 재발급 시 reIssue count를 증가시킨다.
        userRefreshToken.increaseReIssueCount();

        String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY, ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);

        return createAccessToken(user.getId(), username, authorities);

    }

    // 전달된 Refresh token 이 유효한 지 검사하는 메소드
    // 현재 유저가 보유하고 있는 Refresh token 이 맞는 지
    // 그리고 그 Refresh Token 이 access token 을 발급할 수 있는 상태인지(재발급 5회 이하)를 점검한다.
    @Transactional
    public void validateRefreshToken(String refreshToken, String oldAccessToken) throws JsonProcessingException {
        validateTokenIsExpiredOrTampered(refreshToken);

        String username = getPayloadFromJWTToken(oldAccessToken).get("email").toString();

        Users user = userRepository.findByEmail(username).orElseThrow(() -> new IllegalStateException("해당하는 유저정보가 존재하지 않습니다"));

        userRefreshTokenRepository.findByUserAndReIssueCountLessThan(user, ApplicationConstants.REFRESH_TOKEN_RE_ISSUE_LIMIT)
                        .filter(userRefreshToken -> userRefreshToken.validateRefreshToken(refreshToken))
                                .orElseThrow(() -> new RuntimeException("토큰 만료! 다시 로그인하세요"));
    }

    @Transactional
    public void validateRefreshTokenLogout(String refreshToken) throws JsonProcessingException {
        validateTokenIsExpiredOrTampered(refreshToken);
    }

    public void validateTokenIsExpiredOrTampered(String token) {
        try {
            // JWT 토큰 파싱 및 서명 검증
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)  // secretKey로 서명 검증
                    .build()
                    .parseClaimsJws(token);   // 토큰 파싱 및 검증 (서명 + 만료 여부 검증)

        } catch (ExpiredJwtException e) {
            // 만료된 토큰 예외 처리
            throw new ExpiredJwtException(null, null, "JWT Token has expired", e);
        } catch (io.jsonwebtoken.JwtException e) {
            // 변조된 토큰 또는 다른 JWT 관련 예외 처리
            throw new io.jsonwebtoken.JwtException("Invalid or tampered JWT Token", e);
        }
    }


    public Map getPayloadFromJWTToken (String token) throws JsonProcessingException {
        return objectMapper.readValue(
                new String(Base64.getDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8),
                Map.class
        );
    }

}

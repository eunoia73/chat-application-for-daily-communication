package com.one.social_project.domain.user.util;

import com.one.social_project.domain.user.entity.User;
import com.one.social_project.domain.user.repository.UserRepository;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class TokenProvider {
    private final UserRepository userRepository;

    // JWT 비밀 키 (보안을 위해 환경 변수 또는 Spring Secret Store에 저장하는 것이 좋습니다)
    @Value("${spring.jwt.secret}")
    private String secretKey;

    // 토큰 만료 시간 (예시: 1시간)
    private final long validityInMilliseconds = 3600000; // 1 hour
    // Refresh Token의 만료 시간 (예시: 7일)
    private final long refreshValidityInMilliseconds = 604800000; // 7 days

    // JWT 토큰을 발급하는 메서드 (Access Token)
    public String createAccessToken(String email) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validityInMilliseconds);

        // Access Token 생성
        return Jwts.builder()
                .setSubject(email)  // 사용자 메일을 subject로 설정
                .setIssuedAt(now)  // 발급 시간
                .setExpiration(expiryDate)  // 만료 시간 설정
                .signWith(SignatureAlgorithm.HS512, secretKey)  // 서명 알고리즘과 비밀 키
                .compact();
    }

    // JWT 토큰을 발급하는 메서드 (Refresh Token)
    public String createRefreshToken(String email) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshValidityInMilliseconds);

        // Refresh Token 생성
        return Jwts.builder()
                .setSubject(email)  // 사용자 메일을 subject로 설정
                .setIssuedAt(now)  // 발급 시간
                .setExpiration(expiryDate)  // 만료 시간 설정 (7일)
                .signWith(SignatureAlgorithm.HS512, secretKey)  // 서명 알고리즘과 비밀 키
                .compact();
    }

    // JWT 토큰에서 사용자 정보를 추출하는 메서드 (Access Token)
    public String getEmailFromAccessToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();  // subject는 사용자 이메일
    }

    public String getNicknameFromToken(String token) {
        try {
            String email = getEmailFromAccessToken(token);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자가 존재하지 않습니다."));

            // 사용자 닉네임 반환
            return user.getNickname();
        } catch (Exception e) {
            throw new IllegalArgumentException("토큰으로 닉네임 조회 실패: " + e.getMessage(), e);
        }
    }

    public User getUserFromToken(String token) {
        try {
            String email = getEmailFromAccessToken(token);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자가 존재하지 않습니다."));

            // 사용자 닉네임 반환
            return user;
        } catch (Exception e) {
            throw new IllegalArgumentException("토큰으로 닉네임 조회 실패: " + e.getMessage(), e);
        }
    }

    // JWT 토큰에서 사용자 정보를 추출하는 메서드 (Refresh Token)
    public String getEmailFromRefreshToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();  // subject는 사용자 이메일
    }

    // 토큰이 만료되었는지 검증하는 메서드 (Access Token)
    public boolean validateAccessToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // 유효하지 않거나 만료된 토큰인 경우 예외 처리

            return false;
        }
    }

    // 토큰이 만료되었는지 검증하는 메서드 (Refresh Token)
    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // 유효하지 않거나 만료된 토큰인 경우 예외 처리
            return false;
        }
    }
}

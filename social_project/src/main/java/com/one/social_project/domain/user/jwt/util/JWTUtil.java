package com.one.social_project.domain.user.jwt.util;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import io.jsonwebtoken.*;

@Component
public class JWTUtil {

    private Key key;
    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 1; // 1분
    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7일

    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
        byte[] byteSecretKey = Decoders.BASE64.decode(secret);
        key = Keys.hmacShaKeyFor(byteSecretKey);
    }

    // 토큰에서 이메일을 추출
    public String getEmail(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("email", String.class);
    }

    // 토큰에서 역할을 추출
    public String getRole(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("role", String.class);
    }

    // 토큰 만료 여부 확인
    public Boolean isExpired(String token) {
        try {
            Date expirationDate = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            // 만료된 토큰인지 확인
            return expirationDate.before(new Date());
        } catch (ExpiredJwtException e) {
            // 만약 토큰이 만료되었으면 바로 true를 반환 (만료된 토큰임)
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // JWT가 잘못되었거나 파싱 오류가 발생한 경우 처리
            return false;
        }
    }

    // 액세스 토큰 생성
    public String createAccessToken(String email, String role) {
        Claims claims = Jwts.claims();
        claims.put("email", email);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 리프레시 토큰 생성
    public String createRefreshToken(String email, String role) {
        Claims claims = Jwts.claims();
        claims.put("email", email);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 액세스 토큰 재발급 (리프레시 토큰을 사용)
    public String refreshAccessToken(String refreshToken) {
        // 리프레시 토큰 만료 여부 체크
        if (isExpired(refreshToken)) {
            throw new RuntimeException("Refresh token is expired.");
        }

        // 리프레시 토큰에서 사용자 정보 추출
        String email = getEmail(refreshToken);  // 이메일을 email로 사용한다고 가정
        String role = getRole(refreshToken);  // 역할 정보 추출

        // 새로운 액세스 토큰 생성
        return createAccessToken(email, role);
    }
}

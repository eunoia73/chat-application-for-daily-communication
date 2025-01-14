package com.one.social_project.domain.user.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.one.social_project.domain.user.basic.repository.*;
import com.one.social_project.exception.dto.ErrorResponse;
import com.one.social_project.exception.errorCode.UserErrorCode;
import com.one.social_project.exception.exception.UserException;
import io.jsonwebtoken.Jwts;
import com.one.social_project.domain.user.ApplicationConstants;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import com.one.social_project.domain.user.util.RedisSessionManager;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JWTTokenGeneratorFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final RedisSessionManager redisSessionManager;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 만료 시간 (24시간)


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String accessToken = request.getHeader(ApplicationConstants.JWT_HEADER);

        if(redisSessionManager.isTokenBlacklisted(accessToken))
        {
                UserException userException = new UserException(UserErrorCode.LOGOUT_USER);
                ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED, userException.getMessage());
                // JSON 응답을 작성하기 위한 ObjectMapper
                ObjectMapper objectMapper = new ObjectMapper();
                response.setStatus(errorResponse.getStatus().value());  // 401 Unauthorized
                response.setContentType("application/json");
                response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                return;
        }


        if(authentication != null) {
            Environment env = getEnvironment();
            if(env != null) {
                String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY
                        , ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
                SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                String jwt = Jwts.builder()
                        .claim("username", authentication.getName())
                        .claim("authorities", authentication.getAuthorities().stream().map(
                                GrantedAuthority::getAuthority).collect(Collectors.joining(",")))
                        .setIssuedAt(new Date())
                        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                        .signWith(secretKey).compact();

                System.out.println(jwt);

                // 최초 로그인 거짓


                response.setHeader(ApplicationConstants.JWT_HEADER, jwt);
            }
            filterChain.doFilter(request, response);
        }

    }


    // 최초 로그인 시에만 JWTTokenGeneratorFilter가 동작하여 토큰을 생성하여야 한다.
    // 그 이외의 경우는 토큰을 생성하면 안 된다.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // 요청 헤더에서 AccessToken 추출
        String accessToken = request.getHeader(ApplicationConstants.JWT_HEADER);

        redisSessionManager.isTokenBlacklisted(accessToken);

        // accessToken이 비어있거나 null일 경우 첫 로그인 처리
        if (accessToken == null || accessToken.trim().isEmpty()) {
            // 첫 로그인 시 바로 false 리턴하여 필터를 중지시킴
            return false;
        }

        try {
            String email = getPayloadFromJWTToken(accessToken).get("email").toString();
            System.out.println(email);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        System.out.println();


        return true;  // 이 부분은 조건에 맞게 추가
    }

    public Map getPayloadFromJWTToken (String token) throws JsonProcessingException {
        return objectMapper.readValue(
                new String(Base64.getDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8),
                Map.class
        );
    }
}

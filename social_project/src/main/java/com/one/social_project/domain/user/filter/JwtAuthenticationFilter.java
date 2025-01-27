package com.one.social_project.domain.user.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.one.social_project.domain.user.entity.User;
import com.one.social_project.domain.user.service.BlacklistService;
import com.one.social_project.domain.user.util.TokenProvider;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer";
    private static final int BEARER_PREFIX_LENGTH = 6;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final TokenProvider tokenProvider;
    private final BlacklistService blacklistService;

    public JwtAuthenticationFilter(TokenProvider tokenProvider, BlacklistService blacklistService) {
        this.tokenProvider = tokenProvider;
        this.blacklistService = blacklistService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                  @NonNull HttpServletResponse response,
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = getTokenFromRequest(request);
            if (token != null) {
                processToken(token, request, response);
            }
        } catch (Exception e) {
            handleFilterException(e);
        }
        
        filterChain.doFilter(request, response);
    }

    private void processToken(String token, HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        if (isTokenBlacklisted(token, response)) {
            return;
        }

        authenticateToken(token, request);
    }

    private boolean isTokenBlacklisted(String token, HttpServletResponse response) throws IOException {
        if (blacklistService.isTokenBlacklisted(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is blacklisted");
            return true;
        }
        return false;
    }

    private void authenticateToken(String token, HttpServletRequest request) {
        if (tokenProvider.validateAccessToken(token)) {
            User user = tokenProvider.getUserFromToken(token);
            setSecurityContext(user, request);
        }
    }

    private void setSecurityContext(User user, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(user, null, null);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX_LENGTH);
        }
        return null;
    }

    private void handleFilterException(Exception e) {
        if (e instanceof IllegalArgumentException) {
            logger.error("토큰 검증 중 오류 발생: {}", e.getMessage());
        } else if (e instanceof RedisConnectionFailureException) {
            logger.error("Redis 연결 오류 발생: {}", e.getMessage());
        } else {
            logger.error("예상치 못한 오류 발생: {}", e.getMessage());
        }
    }
}

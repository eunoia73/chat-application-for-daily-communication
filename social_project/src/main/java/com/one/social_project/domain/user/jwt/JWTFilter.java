package com.one.social_project.domain.user.jwt;


import com.one.social_project.domain.user.dto.CustomUserDetails;
import com.one.social_project.domain.user.entity.UserEntity;
import com.one.social_project.domain.user.jwt.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {

        this.jwtUtil = jwtUtil;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //request에서 Authorization 헤더를 찾음
        String authorization= request.getHeader("Authorization");
        System.out.println(authorization);

        //Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {

            System.out.println("token null");
            filterChain.doFilter(request, response);

            //조건이 해당되면 메소드 종료 (필수)
            return;
        }

        String[] tokens = authorization.split(" ");

        // 분리된 결과 출력
            String tokenType = tokens[0]; // Bearer
            String accessToken = tokens[1]; // 첫 번째 JWT 토큰
            String refreshToken = tokens[2]; // 두 번째 JWT 토큰

            System.out.println("Token Type: " + tokenType);
            System.out.println("Access Token: " + accessToken);
            System.out.println("Refresh Token: " + refreshToken);



        boolean isAccessTokenExpired = jwtUtil.isExpired(accessToken);//액세스 토큰이 만료
        boolean isRefreshTokenExpired = jwtUtil.isExpired(refreshToken);

        System.out.println("액세스 토큰 만료 여부 : " + isAccessTokenExpired);
        //토큰 소멸 시간 검증
        if (isAccessTokenExpired)
        {
            if (!isRefreshTokenExpired)
            {
                System.out.println("토큰 재발급");
                accessToken = jwtUtil.refreshAccessToken(refreshToken);
            }
            else {
                System.out.println("token expired");
                filterChain.doFilter(request, response);//조건이 해당되면 메소드 종료 (필수)
                return;
            }

        }

        //토큰에서 email과 role 획득
        String email = jwtUtil.getEmail(accessToken);
        String role = jwtUtil.getRole(accessToken);
        System.out.println("email: " + email + ", role: " + role);

        //userEntity를 생성하여 값 set
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(email);
        userEntity.setPassword("temppassword");
        userEntity.setRole(role);

        //UserDetails에 회원 정보 객체 담기
        CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);
        System.out.println("customUserDetails: " + customUserDetails.getUsername());

        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        //세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);
        response.addHeader("Authorization", "Bearer " + accessToken + " " + refreshToken);

        filterChain.doFilter(request, response);
    }
}
package com.one.social_project.domain.user.util.config;

import com.one.social_project.domain.user.service.AuthService;
import com.one.social_project.domain.user.service.CustomOAuth2UserService;
import com.one.social_project.domain.user.service.UserService;
import com.one.social_project.domain.user.util.CustomBasicAuthenticationEntryPoint;
import com.one.social_project.domain.user.util.Handler.CustomAccessDeniedHandler;
import com.one.social_project.domain.user.util.Handler.OAuth2SuccessHandler;
import com.one.social_project.domain.user.util.RedisSessionManager;
import com.one.social_project.domain.user.filter.JwtAuthenticationFilter;
import com.one.social_project.domain.user.util.TokenProvider;
import com.one.social_project.domain.user.util.logout.CustomLogoutSuccessHandler;
import com.one.social_project.domain.user.repository.UserRepository;
import com.one.social_project.domain.user.util.properties.GoogleOAuth2Properties;
import com.one.social_project.domain.user.util.properties.KakaoOAuth2Properties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.filters.CorsFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RedisSessionManager redisSessionManager;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserRepository userRepository;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final TokenProvider tokenProvider;
    private final KakaoOAuth2Properties kakaoOAuth2Properties;
    private final GoogleOAuth2Properties googleOAuth2Properties;

    // 인증과정 없이 요청 가능한 url
    String[] urlsToBePermittedAll = {
            "/hello",
            "/api/register",
            "/api/login",
            "/api/chat/**",
            "/chat/**",
            "/h2-console/**",
            "/files/**",
            "/api/login/oauth2/**"};

    // 인증 과정이 필요하여
    // 인증 없이 요청한 경우 401 Error 반환
    String[] urlsToBeAuthenticated = {"/api/logout", "/users/password/**"};


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .requiresChannel(rcc -> rcc.anyRequest().requiresInsecure())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(corsConfig -> corsConfig.configurationSource(new CorsConfigurationSource() {
                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration config = new CorsConfiguration();
                        config.setAllowedOrigins(Collections.singletonList("http://localhost:5173"));
                        config.setAllowCredentials(true); // 쿠키 사용 허용
                        config.setAllowedMethods(Collections.singletonList("*"));
                        config.setAllowCredentials(true);
                        config.setAllowedHeaders(Collections.singletonList("*"));
                        config.setExposedHeaders(List.of("Authorization"));
                        config.setMaxAge(3600L);
                        return config;
                    }
                }))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(urlsToBePermittedAll).permitAll()
                        .requestMatchers(urlsToBeAuthenticated).authenticated()
                        .anyRequest().permitAll())
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(hbc -> hbc.authenticationEntryPoint(new CustomBasicAuthenticationEntryPoint()))
                .exceptionHandling(ehc -> ehc
                        .accessDeniedHandler(new CustomAccessDeniedHandler()))
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .addLogoutHandler(new CustomLogoutSuccessHandler(redisSessionManager, tokenProvider, userRepository))
                        .logoutSuccessHandler(new CustomLogoutSuccessHandler(redisSessionManager, tokenProvider, userRepository))
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "Authorization")
                        .permitAll())
                .oauth2Login(oauth -> oauth
                        .successHandler(oAuth2SuccessHandler)
                        .userInfoEndpoint(user -> user
                                .userService(oAuth2UserService())));

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // apiLogin이라는 사용자 정의 인증 로직을 위해서는, 요청이 들어올 때 authentication process를 시작하도록 하여야 한다.
    // 그렇게 하기 위해서는 Authentication Manager를 구현해야 함
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, AuthService authService) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder
                .userDetailsService(authService)
                .passwordEncoder(passwordEncoder());

        return authenticationManagerBuilder.build();
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService() {
        return new CustomOAuth2UserService(googleOAuth2Properties, kakaoOAuth2Properties);  // 사용자 정보 처리할 서비스
    }
}

package com.one.social_project.domain.user.util.config;

import com.one.social_project.domain.user.service.CustomOAuth2UserService;
import com.one.social_project.domain.user.util.CustomBasicAuthenticationEntryPoint;
import com.one.social_project.domain.user.util.Handler.CustomAccessDeniedHandler;
import com.one.social_project.domain.user.util.Handler.OAuth2SuccessHandler;
import com.one.social_project.domain.user.util.RedisSessionManager;
import com.one.social_project.domain.user.filter.JwtAuthenticationFilter;
import com.one.social_project.domain.user.util.TokenProvider;
import com.one.social_project.domain.user.util.logout.CustomLogoutSuccessHandler;
import com.one.social_project.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import java.util.Collections;
import java.util.List;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisSessionManager redisSessionManager;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserRepository userRepository;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final TokenProvider tokenProvider;

    // 인증과정 없이 요청 가능한 url
    String[] urlsToBePermittedAll = {"/hello", "/api/login", "/h2-console/**", "/**", "/files/**"};

    // 인증 과정이 필요하여
    // 인증 없이 요청한 경우 401 Error 반환
    String[] urlsToBeAuthenticated = {"/api/logout", "/users/password/**"};


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 개발 단계에서는 오로지 HTTP 만을 이용해서 통신하도록 설정
                .requiresChannel(rcc -> rcc.anyRequest().requiresInsecure())
                // CSRF 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                // JWT 토큰 시스템을 사용하기 위해 jsessionid 발급을 중단.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // CORS 설정
                .cors(corsConfig -> corsConfig.configurationSource(new CorsConfigurationSource() {
                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration config = new CorsConfiguration();
                        config.setAllowedOrigins(Collections.singletonList("http://localhost:5173"));
                        config.setAllowedMethods(Collections.singletonList("*"));
                        config.setAllowCredentials(true);
                        config.setAllowedHeaders(Collections.singletonList("*"));
                        config.setExposedHeaders(List.of("Authorization"));
                        config.setMaxAge(3600L);
                        return config;
                    }
                }))
                // 인증이 필요한 url 과 그렇지 않은 url 설정
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/files/**", "/api/users", "/api/login", "/api/login/oauth2").permitAll()
                        .requestMatchers(urlsToBeAuthenticated).authenticated()
                        .anyRequest().permitAll()
                )
                //.addFilterBefore(jwtTokenValidatorFilter, BasicAuthenticationFilter.class)
                // X-Frame-Options 헤더설정 for h2-database console
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 전에 실행되도록 설정
        // Basic Authentication 이용한 인증작업 실패 시 어떠한 루틴이 실행될 것인가 설정.
                .httpBasic(hbc -> hbc.authenticationEntryPoint(new CustomBasicAuthenticationEntryPoint()))

                // 403 Forbidden Error 발생 시 어떠한 루틴이 실행될 것인가 설정.
                .exceptionHandling(ehc -> ehc
                        .accessDeniedHandler(new CustomAccessDeniedHandler()))

                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .addLogoutHandler(
                                new CustomLogoutSuccessHandler(
                                        redisSessionManager,
                                        tokenProvider,
                                        userRepository)
                        )
                        .logoutSuccessHandler(
                                new CustomLogoutSuccessHandler(
                                        redisSessionManager,
                                        tokenProvider,
                                        userRepository)
                        )
                        .invalidateHttpSession(true) // 세션 무효화
                        .clearAuthentication(true)  // 인증 정보 삭제
                        .deleteCookies("JSESSIONID", "Authorization") // 쿠키 삭제
                        .permitAll()
                );

        http
                .oauth2Login()
                .successHandler(oAuth2SuccessHandler)
                .userInfoEndpoint()
                .userService(oAuth2UserService());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // apiLogin이라는 사용자 정의 인증 로직을 위해서는, 요청이 들어올 때 authentication process를 시작하도록 하여야 한다.
    // 그렇게 하기 위해서는 Authentication Manager를 구현해야 함
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
//        CustomAuthenticationProvider authenticationProvider =
//                new CustomAuthenticationProvider(userDetailsService, passwordEncoder, redisTemplate);
//        ProviderManager providerManager = new ProviderManager(authenticationProvider);
//        providerManager.setEraseCredentialsAfterAuthentication(false); // 인증과정에서 authentication객체의 비밀번호를 지우지 않고 넘겨주어서 사용자 정의 인증로직이 제대로 동작하게 함.
        return  null;
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService() {
        return new CustomOAuth2UserService(userRepository);  // 사용자 정보 처리할 서비스
    }
}

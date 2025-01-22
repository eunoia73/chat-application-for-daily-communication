package com.one.social_project.domain.user.service;

import com.one.social_project.domain.user.dto.OAuth2UserInfo;
import com.one.social_project.domain.user.dto.login.LoginReqDto;
import com.one.social_project.domain.user.dto.login.LoginResDto;
import com.one.social_project.domain.user.dto.register.RegisterReqDto;
import com.one.social_project.domain.user.dto.register.RegisterResDto;
import com.one.social_project.domain.user.entity.User;
import com.one.social_project.domain.user.repository.UserRepository;
import com.one.social_project.domain.user.util.CustomUserDetails;
import com.one.social_project.domain.user.util.RedisSessionManager;
import com.one.social_project.domain.user.util.TokenProvider;
import com.one.social_project.exception.errorCode.UserErrorCode;
import com.one.social_project.exception.exception.UserException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {


    private final RedisSessionManager redisSessionManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public boolean isRegistered(String email) {
        return userRepository.existsByEmail(email);
    }



    // 회원가입
    @Transactional
    public RegisterResDto register(RegisterReqDto registerReqDto) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(registerReqDto.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }
        if(registerReqDto.getOauth2UserInfo() != null)
        {
            if(registerReqDto.getOauth2UserInfo().getProvider().equals("google")) {
                OAuth2UserInfo oAuth2UserInfo = registerReqDto.getOauth2UserInfo();
                // User 객체 생성 및 저장
                User newUser = new User(registerReqDto.getEmail(),
                        registerReqDto.getNickname(),
                        "google",
                        "ROLE_USER",
                        oAuth2UserInfo.getOauthId(),
                        true);
                userRepository.save(newUser);
            }
        }
        else {


            // 비밀번호 암호화
            String encodedPassword = passwordEncoder.encode(registerReqDto.getPassword());


            // User 객체 생성 및 저장
            User newUser = new User(registerReqDto.getEmail(),
                    encodedPassword,
                    registerReqDto.getNickname(),
                    true,
                    "ROLE_USER",
                    true);
            userRepository.save(newUser);
        }

        return new RegisterResDto("register success!");
    }



    // 로그인
    public LoginResDto login(LoginReqDto loginReqDto, HttpServletResponse response) {

        if(redisSessionManager.isTokenBlacklisted(loginReqDto.getAccessToken()))
        {
            throw new UserException(UserErrorCode.LOGOUT_USER);
        }
        // 이메일로 사용자 찾기
        User user = userRepository.findByEmail(loginReqDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 비밀번호 검증
        if(!loginReqDto.getIsSocialLogin())
        {
            if ( !passwordEncoder.matches(loginReqDto.getPassword(), user.getPassword())) {
                throw new IllegalArgumentException("잘못된 비밀번호입니다.");
            }
        }

        // 로그인 성공 시 JWT 토큰 발급
        String accessToken = tokenProvider.createAccessToken(loginReqDto.getEmail());
        String refreshToken = tokenProvider.createRefreshToken(loginReqDto.getEmail());

        response.addCookie(createCookie("Authorization", accessToken, true));
// HttpServletResponse에 Authorization 헤더 적용
        response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer" + accessToken);

        return new LoginResDto(loginReqDto.getEmail(), accessToken, refreshToken);
    }

    public void logout()
    {

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
        return new CustomUserDetails(user, null); // 여기에서 CustomUserDetails를 사용
    }

    private Cookie createCookie(String key, String value, boolean isHttpOnly) {

        Cookie cookie = new Cookie(key, "Bearer" + value);
        cookie.setMaxAge(60*60*60);
        //cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(isHttpOnly);

        return cookie;
    }
}

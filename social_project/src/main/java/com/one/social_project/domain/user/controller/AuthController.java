package com.one.social_project.domain.user.controller;

import com.one.social_project.domain.user.dto.OAuth2UserInfo;
import com.one.social_project.domain.user.dto.login.LoginReqDto;
import com.one.social_project.domain.user.dto.login.LoginResDto;
import com.one.social_project.domain.user.dto.register.RegisterReqDto;
import com.one.social_project.domain.user.dto.register.RegisterResDto;
import com.one.social_project.domain.user.entity.User;
import com.one.social_project.domain.user.service.AuthService;
import com.one.social_project.domain.user.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final CustomOAuth2UserService customOAuth2UserService;

    @PostMapping("/login")
    public ResponseEntity<LoginResDto> login(@RequestBody LoginReqDto loginReqDto, HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(loginReqDto, response));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResDto> register(@RequestBody RegisterReqDto registerReqDto) {
        return ResponseEntity.ok(authService.register(registerReqDto));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/login/oauth/code/{provider}")
    public ResponseEntity<LoginResDto> loginWithOAuth(@RequestParam("code") String code, @PathVariable("provider") String provider, HttpServletResponse response) {
        OAuth2UserInfo oAuth2UserInfo = customOAuth2UserService.getUserInfoFromOAuth(provider, code);
        oAuth2UserInfo.setProvider(provider);

        if (!authService.isRegistered(oAuth2UserInfo.getEmail())) {
            RegisterReqDto registerReqDto = new RegisterReqDto();
            registerReqDto.setEmail(oAuth2UserInfo.getEmail());
            registerReqDto.setNickname(oAuth2UserInfo.getNickname());
            registerReqDto.setOauth2UserInfo(oAuth2UserInfo);
            authService.register(registerReqDto);
        }

        LoginReqDto loginReqDto = new LoginReqDto();
        loginReqDto.setEmail(oAuth2UserInfo.getEmail());
        loginReqDto.setIsSocialLogin(true);

        return ResponseEntity.ok(authService.login(loginReqDto, response));
    }

    @GetMapping("/test")
    public ResponseEntity<String> test(@AuthenticationPrincipal User customUserDetails) {
        return ResponseEntity.ok(customUserDetails.getEmail());
    }
}

package com.one.social_project.domain.user.controller;

import com.one.social_project.domain.user.dto.OAuth2UserInfo;
import com.one.social_project.domain.user.dto.login.LoginReqDto;
import com.one.social_project.domain.user.dto.login.LoginResDto;
import com.one.social_project.domain.user.dto.register.RegisterReqDto;
import com.one.social_project.domain.user.dto.register.RegisterResDto;
import com.one.social_project.domain.user.service.CustomOAuth2UserService;
import com.one.social_project.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

    private final UserService userService;
    private final CustomOAuth2UserService customOAuth2UserService;

    @PostMapping("/login")
    public ResponseEntity<LoginResDto> login(@RequestBody LoginReqDto loginReqDto) {
        return ResponseEntity.ok(userService.login(loginReqDto));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResDto> register(@RequestBody RegisterReqDto registerReqDto) {
        return ResponseEntity.ok(userService.register(registerReqDto));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout()
    {
        userService.logout();
        return ResponseEntity.ok().build();
    }



    @GetMapping("/login/oauth2/code/google")
    public ResponseEntity<LoginResDto> loginGoogle(@RequestParam("code") String code) {

        String googleAccessToken = customOAuth2UserService.getAccessToken(code);
        OAuth2UserInfo oAuth2UserInfo = customOAuth2UserService.getUserInfoFromGoogle(googleAccessToken);
        oAuth2UserInfo.setProvider("google");

        if(!userService.isRegistered(oAuth2UserInfo.getEmail()))
        {
            RegisterReqDto registerReqDto = new RegisterReqDto();
            registerReqDto.setEmail(oAuth2UserInfo.getEmail());
            registerReqDto.setNickname(oAuth2UserInfo.getNickname());
            registerReqDto.setOauth2UserInfo(oAuth2UserInfo);
            userService.register(registerReqDto);
        }
        LoginReqDto loginReqDto = new LoginReqDto();
        loginReqDto.setEmail(oAuth2UserInfo.getEmail());
        loginReqDto.setIsSocialLogin(true);

        return ResponseEntity.ok(userService.login(loginReqDto));
    }
}

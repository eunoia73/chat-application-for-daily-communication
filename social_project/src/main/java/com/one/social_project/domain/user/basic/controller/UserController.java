package com.one.social_project.domain.user.basic.controller;

import com.one.social_project.domain.user.basic.service.UserService;
import com.one.social_project.domain.user.basic.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.one.social_project.domain.user.util.CustomUserDetails;
import org.springframework.security.access.prepost.PreAuthorize;

import java.nio.file.AccessDeniedException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PreAuthorize("hasAnyRole('user', 'admin')")
    @GetMapping("/users")
    public ResponseEntity<UserDto> mypage(@AuthenticationPrincipal CustomUserDetails userDetails) throws AccessDeniedException {
        return ResponseEntity.ok(userService.mypage(userDetails));
    }

    // 회원 가입 처리
    @PostMapping("/users")
    public ResponseEntity<UserDto> register(@RequestBody UserRegisterDto userRegisterDto) {
        String hashedPassword = passwordEncoder.encode(userRegisterDto.getPassword());
        userRegisterDto.setPassword(hashedPassword);

        UserDto result = userService.register(userRegisterDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }


    @PostMapping("/login")
    public ResponseEntity<SignInResponse> apiLogin(@RequestBody SignInRequest signInRequest) {

        SignInResponse signInResponse = userService.signIn(signInRequest);

        return ResponseEntity.status(HttpStatus.OK)
                .body(signInResponse);
    }


    @PatchMapping("/users/password")
    public ResponseEntity<String> updateUserInformation(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody PasswordChangeRequest passwordChangeRequest) {

        userService.changePassword(customUserDetails.getId(), passwordChangeRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
    }

    @GetMapping("/test")
    public String test(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        System.out.println("customUserDetails = " + customUserDetails);
        System.out.println("nickname" + customUserDetails.getNickname());
        return "str";
    }

}

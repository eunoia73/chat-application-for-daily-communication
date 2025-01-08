package com.one.social_project.domain.user.controller;


import com.one.social_project.domain.user.dto.CustomUserDetails;
import com.one.social_project.domain.user.dto.JoinDto;
import com.one.social_project.domain.user.dto.UserDto;
import com.one.social_project.domain.user.entity.UserEntity;
import com.one.social_project.domain.user.service.CustomUserDetailsService;
import com.one.social_project.domain.user.service.JoinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class JoinController {

private final JoinService joinService;
private final CustomUserDetailsService customUserDetailsService;

    @PostMapping("/join")
    public ResponseEntity<UserDto> join(@Valid @RequestBody JoinDto joinDto) throws Exception {
        return ResponseEntity.ok(joinService.joinProcess(joinDto));
    }

    @GetMapping("/admin")
    public String admin(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        System.out.printf("userDetails: %s", customUserDetails.toString());
        return "admin";
    }


}

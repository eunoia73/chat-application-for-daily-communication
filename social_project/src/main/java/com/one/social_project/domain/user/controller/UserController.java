package com.one.social_project.domain.user.controller;

import com.one.social_project.domain.user.dto.user.UserDto;
import com.one.social_project.domain.user.entity.User;
import com.one.social_project.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/")
    public ResponseEntity<UserDto> getUserInfo(@AuthenticationPrincipal User user) {

        UserDto userDto = userService.getUserInfo(user);
        return ResponseEntity.ok(userDto);
    }

    @PatchMapping("/profileImage")
    public ResponseEntity<String> changeProfileImage(@AuthenticationPrincipal User user, @RequestParam String imageUrl) {
        return ResponseEntity.ok(userService.changeProfileImage(user.getId(), imageUrl));
    }


}

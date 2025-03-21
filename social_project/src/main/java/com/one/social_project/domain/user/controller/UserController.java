package com.one.social_project.domain.user.controller;

import com.one.social_project.domain.user.dto.response.UserDtoResponse;
import com.one.social_project.domain.user.dto.user.UserDto;
import com.one.social_project.domain.user.dto.util.CheckDto;
import com.one.social_project.domain.user.entity.User;
import com.one.social_project.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PatchMapping("/profileImage/{imgUrl}")
    public ResponseEntity<String> changeProfileImage(@AuthenticationPrincipal User user, @PathVariable("imgUrl") String imageUrl) {
        return ResponseEntity.ok(userService.changeProfileImage(user.getId(), imageUrl));
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserDtoResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/check/email/{email}")
    public ResponseEntity<CheckDto> checkEmail(@PathVariable("email") String email) {
        return ResponseEntity.ok(userService.isValidEmail(email));
    }

    @GetMapping("/check/nickname/{nickname}")
    public ResponseEntity<CheckDto> checkNickname(@PathVariable("nickname") String nickname) {
        return ResponseEntity.ok(userService.isValidNickname(nickname));
    }
}

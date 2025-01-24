package com.one.social_project.domain.friend.controller;

import com.one.social_project.domain.friend.service.FriendshipService;
import com.one.social_project.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/friendship")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    @PostMapping("/{email}")
    public ResponseEntity<String> createFriendship(@AuthenticationPrincipal User user, @PathVariable("email") String email) throws Exception {
        friendshipService.createFriendship(user.getEmail(), email);

        return ResponseEntity.ok("친구요청을 보냈습니다!");
    }

    @GetMapping("/waits")
    public ResponseEntity<?> getWaitings(@AuthenticationPrincipal User user) throws Exception {
        return friendshipService.getWaitingFriendList(user.getEmail());
    }

    @GetMapping("/accepts")
    public ResponseEntity<?> getAccepts(@AuthenticationPrincipal User user) throws Exception {
        return friendshipService.getAcceptFriendList(user.getEmail());
    }

    @PostMapping("/approve/{id}")
    @ResponseStatus(HttpStatus.OK)
    public String approveFriendship (@Valid @PathVariable("id") Long friendshipId) throws Exception{
        return friendshipService.approveFriendshipRequest(friendshipId);
    }
}

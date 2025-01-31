package com.one.social_project.domain.friend.controller;

import com.one.social_project.domain.email.dto.util.ResultDto;
import com.one.social_project.domain.friend.dto.FriendshipResDto;
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
    public ResponseEntity<FriendshipResDto> createFriendship(@AuthenticationPrincipal User user, @PathVariable("email") String email) throws Exception {
        friendshipService.createFriendship(user.getEmail(), email);

        FriendshipResDto friendshipResDto = new FriendshipResDto();
        friendshipResDto.setMessage("친구요청을 보냈습니다!");

        return ResponseEntity.ok(friendshipResDto);
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

    @DeleteMapping("/{email}")
    public ResponseEntity<?> deleteFriendship (@AuthenticationPrincipal User user, @PathVariable("email") String email) throws Exception {
        friendshipService.deleteFriendship(user.getEmail(), email);
        FriendshipResDto friendshipResDto = new FriendshipResDto();
        friendshipResDto.setMessage("친구를 삭제했습니다!");
        return ResponseEntity.ok(friendshipResDto);
    }
}

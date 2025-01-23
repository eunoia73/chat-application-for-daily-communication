package com.one.social_project.domain.notifications.controller;

import com.one.social_project.domain.notifications.dto.NotificationListDTO;
import com.one.social_project.domain.notifications.service.NotificationService;
import com.one.social_project.domain.search.PageableResponse;
import com.one.social_project.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    //유저별 모든 알림 조회
    @GetMapping("/api/users/notifications")
    public ResponseEntity<?> getNotifications(@AuthenticationPrincipal User user, Pageable pageable) {
        String userNickname = user.getNickname();
        Page<NotificationListDTO> result = notificationService.getNotificationByUserNickname(userNickname, pageable);

        //PageableResponse 객체 생성
        PageableResponse pageableResponse = new PageableResponse(
                result.getNumber(),
                result.getSize(),
                pageable.getOffset(),
                result.isLast()
        );

        // Map으로 응답 구성
        Map<String, Object> response = new HashMap<>();
        response.put("content", result.getContent());
        response.put("pageable", pageableResponse);

        return ResponseEntity.ok(response);
    }

//
//    //상세 알림 조회
//    @GetMapping("/api/users/notifications/{id}")
//    public ResponseEntity<?> getDetailNotification(@AuthenticationPrincipal User user,
//                                                   @PathVariable("id") Long id) {
//
//        String userNickname = user.getNickname();
//        NotificationDTO detailNotification = notificationService.getDetailNotification(userNickname, id);
//
//        // 알림이 없는 경우
//        if (detailNotification == null) {
//            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("알림이 없습니다.");
//        }
//
//        return ResponseEntity.ok(detailNotification);
//    }

}

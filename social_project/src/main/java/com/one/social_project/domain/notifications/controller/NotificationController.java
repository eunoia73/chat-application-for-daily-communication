package com.one.social_project.domain.notifications.controller;

import com.one.social_project.domain.notifications.dto.NotificationDTO;
import com.one.social_project.domain.notifications.dto.NotificationListDTO;
import com.one.social_project.domain.notifications.service.NotificationService;
import com.one.social_project.domain.user.entity.User;
import com.one.social_project.domain.user.util.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    //유저별 모든 알림 조회
    @GetMapping("/api/users/notifications")
    public ResponseEntity<?> getNotifications(@AuthenticationPrincipal User user) {
        String userNickname = user.getNickname();
        List<NotificationListDTO> notifications = notificationService.getNotificationByUserNickname(userNickname);

        // 알림이 없는 경우
        if (notifications.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("알림이 없습니다.");
        }

        return ResponseEntity.ok(notifications);
    }

    //상세 알림 조회
    @GetMapping("/api/users/notifications/{id}")
    public ResponseEntity<?> getDetailNotification(@AuthenticationPrincipal User user,
                                                   @PathVariable("id") Long id) {

        String userNickname = user.getNickname();
        NotificationDTO detailNotification = notificationService.getDetailNotification(userNickname, id);

        // 알림이 없는 경우
        if (detailNotification == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("알림이 없습니다.");
        }

        return ResponseEntity.ok(detailNotification);
    }

}

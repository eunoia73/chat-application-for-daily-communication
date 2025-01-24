package com.one.social_project.domain.notifications.service;

import com.one.social_project.domain.chat.dto.ChatRoomDTO;
import com.one.social_project.domain.notifications.dto.NotificationDetailDTO;
import com.one.social_project.domain.notifications.dto.NotificationListDTO;
import com.one.social_project.domain.notifications.entity.Notification;
import com.one.social_project.domain.notifications.error.NotificationNotFoundException;
import com.one.social_project.domain.notifications.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    //알림 생성 및 저장
    public void generateNotifications(ChatRoomDTO response) {
        List<String> participants = response.getParticipants();
        //채팅방 생성자는 알림을 받을 필요가 없음
        participants.removeIf(participant -> participant.equals(response.getOwnerId()));

        for (String userId : participants) {
            String notificationId = UUID.randomUUID().toString();
            String message = String.format(
                    "새로운 채팅방에 초대되었습니다 : '%s'", response.getRoomName());
            Notification notification = Notification.builder()
                    .notificationId(notificationId)
                    .receiver(userId)
                    .sender(response.getOwnerId())
                    .message(message)
                    .roomId(response.getRoomId())
                    .roomType(response.getRoomType())
                    .roomName(response.getRoomName())
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);  // DB에 알림 저장
        }
    }

    //유저별 모든 알림 조회
    public Page<NotificationListDTO> getNotificationByUserNickname(String userNickname, Pageable pageable) {
        return notificationRepository.findByReceiver(userNickname, pageable);
    }

    //상세 알림 조회
    public NotificationDetailDTO getDetailNotification(String userNickname, String notificationId) {

        // 알림을 데이터베이스에서 조회
        Notification notification = notificationRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("알림을 찾을 수 없습니다."));

        // 알림이 해당 사용자에게 속하는지 확인
        if (!notification.getReceiver().equals(userNickname)) {
            throw new NotificationNotFoundException("알림을 찾을 수 없습니다.");
        }

        // 알림이 읽지 않은 상태라면 읽음 처리
        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);  // DB에 저장 (isRead 상태 업데이트)
        }

        // Entity -> DTO 변환
        NotificationDetailDTO notificationDTO = NotificationDetailDTO.builder()
                .id(notification.getId())
                .notificationId(notification.getNotificationId())
                .receiver(notification.getReceiver())
                .sender(notification.getSender())
                .message(notification.getMessage())
                .roomId(notification.getRoomId())
                .roomType(notification.getRoomType())
                .roomName(notification.getRoomName())
                .isRead(true)
                .createdAt(notification.getCreatedAt())
                .build();

        return notificationDTO;

    }

}

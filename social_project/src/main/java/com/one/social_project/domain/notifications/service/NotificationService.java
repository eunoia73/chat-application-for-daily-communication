package com.one.social_project.domain.notifications.service;

import com.one.social_project.domain.chat.dto.ChatRoomDTO;
import com.one.social_project.domain.notifications.dto.NotificationDTO;
import com.one.social_project.domain.notifications.dto.NotificationListDTO;
import com.one.social_project.domain.notifications.entity.Notification;
import com.one.social_project.domain.notifications.error.NotificationNotFoundException;
import com.one.social_project.domain.notifications.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
            String message = String.format(
                    "새로운 채팅방에 초대되었습니다 : '%s'", response.getRoomName());
            Notification notification = Notification.builder()
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
    public List<NotificationListDTO> getNotificationByUserNickname(String userNickname) {
        List<Notification> notifications = notificationRepository.findByReceiver(userNickname);

        // Entity -> DTO 변환
        return notifications.stream()
                .map(notification -> NotificationListDTO.builder()
                        .id(notification.getId())
                        .receiver(notification.getReceiver())
                        .sender(notification.getSender())
                        .message(notification.getMessage())
                        .isRead(notification.isRead())
                        .createdAt(notification.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    //상세 알림 조회
    public NotificationDTO getDetailNotification(String userNickname, Long id) {

        // 알림을 데이터베이스에서 조회
        Notification notification = notificationRepository.findById(id)
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
        NotificationDTO notificationDTO = NotificationDTO.builder()
                .id(notification.getId())
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

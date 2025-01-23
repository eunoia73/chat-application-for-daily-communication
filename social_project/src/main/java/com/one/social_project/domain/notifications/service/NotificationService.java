package com.one.social_project.domain.notifications.service;

import com.one.social_project.domain.chat.dto.ChatRoomDTO;
import com.one.social_project.domain.notifications.entity.Notification;
import com.one.social_project.domain.notifications.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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

}

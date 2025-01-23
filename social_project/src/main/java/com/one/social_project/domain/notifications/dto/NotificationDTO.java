package com.one.social_project.domain.notifications.dto;

import com.one.social_project.domain.chat.constant.ChatRoomType;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NotificationDTO {
    private Long id;
    private String receiver; // 알림 수신자
    private String sender;  //알림 발신자
    private String message;
    private String roomId; // 관련된 채팅방 ID
    private ChatRoomType roomType;
    private String roomName;
    private boolean isRead; // 읽음 여부

    private LocalDateTime createdAt;
}

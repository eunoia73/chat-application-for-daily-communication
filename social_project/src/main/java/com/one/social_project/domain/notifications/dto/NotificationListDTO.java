package com.one.social_project.domain.notifications.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NotificationListDTO {
    //유저별 모든 알림 list 보여주기 위한 DTO
    private Long id;
    private String receiver; // 알림 수신자
    private String sender;  //알림 발신자
    private String message;
    private boolean isRead; // 읽음 여부

    private LocalDateTime createdAt;
}

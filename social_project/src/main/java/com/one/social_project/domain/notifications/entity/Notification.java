package com.one.social_project.domain.notifications.entity;

import com.one.social_project.domain.chat.constant.ChatRoomType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//@ToString(callSuper = true)
//@SuperBuilder
//@EntityListeners(AuditingEntityListener.class)
@Builder
@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String receiver; // 알림 수신자
    private String sender;  //알림 발신자
    private String message;
    private String roomId; // 관련된 채팅방 ID

    @Enumerated(EnumType.STRING)
    private ChatRoomType roomType;
    private String roomName;
    private boolean isRead; // 읽음 여부

    @CreatedDate
    private LocalDateTime createdAt;
}

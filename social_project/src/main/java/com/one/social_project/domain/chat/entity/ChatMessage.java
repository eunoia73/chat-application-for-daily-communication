package com.one.social_project.domain.chat.entity;

import jakarta.persistence.Column;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Document(collection = "chat") // MongoDB 컬렉션 이름 설정
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    private String id; // MongoDB는 기본적으로 String ID를 사용합니다.

    @Column(nullable = false)
    private String sender;      // 발신자

    @Column(nullable = false)
    private String message;     // 메시지 내용

    @Column(nullable = false)
    private LocalDateTime createdAt; // 메시지 생성 시간
    private String roomId; // 채팅방 ID (연관 관계 대신 필드로 관리)

}

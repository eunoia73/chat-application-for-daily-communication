package com.one.social_project.domain.chat.entity;

import jakarta.persistence.Column;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "chat") // MongoDB 컬렉션 이름 설정
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    private String id; // MongoDB는 기본적으로 String ID를 사용합니다.

    private String sender;      // 발신자

    private String message;     // 메시지 내용

    private LocalDateTime createdAt; // 메시지 생성 시간

    private String roomId; // 채팅방 ID (연관 관계 대신 필드로 관리)

    private List<String> readers; // 읽은 유저 목록

    private String originFileUrl;

    private String thumbnailUrl;

}

package com.one.social_project.domain.chat.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;            // Primary Key

    @Column(nullable = false)
    private String sender;      // 발신자

    @Column(nullable = false)
    private String message;     // 메시지 내용

    @Column(nullable = false)
    private LocalDateTime createdAt; // 메시지 생성 시간

    // 연관된 채팅방
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @JsonBackReference // 자식 역할의 필드에 붙입니다.
    private ChatRoom chatRoom;
}

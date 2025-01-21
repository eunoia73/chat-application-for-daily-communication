package com.one.social_project.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "read_receipt")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String messageId; // 읽음 상태와 연결된 메시지 ID

    private String userId; // 메시지를 읽은 사용자 ID

    private LocalDateTime readAt; // 메시지를 읽은 시간
}

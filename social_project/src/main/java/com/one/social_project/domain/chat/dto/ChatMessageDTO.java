package com.one.social_project.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {
    private String chatType; // 메시지 유형(Chat, Read_Receipt)
    private String roomId;
    private String sender;
    private String message;
    private LocalDateTime createdAt;
}


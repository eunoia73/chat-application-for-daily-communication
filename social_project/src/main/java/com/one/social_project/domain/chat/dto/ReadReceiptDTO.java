package com.one.social_project.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadReceiptDTO {
    private String roomId;   // 채팅방 ID
    private String message; // 메시지 ID
    private String sender;   // 요청을 보낸 사용자
    private List<String> readers; // 메시지를 읽은 사용자 목록
    private LocalDateTime readAt;
}
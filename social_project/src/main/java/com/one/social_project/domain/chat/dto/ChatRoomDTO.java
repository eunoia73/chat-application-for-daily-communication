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
public class ChatRoomDTO {
    private String roomId; // 채팅방 아이디
    private String roomName; // 채팅방 이름
    private LocalDateTime createdAt;
    private String lastMessage; // 최근 메시지
}

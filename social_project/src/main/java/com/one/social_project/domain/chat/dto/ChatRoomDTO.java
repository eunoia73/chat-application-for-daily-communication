package com.one.social_project.domain.chat.dto;

import com.one.social_project.domain.chat.constant.ChatRoomType;
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
public class ChatRoomDTO {
    private String roomId; // 채팅방 아이디
    private String roomName; // 채팅방 이름
    private String ownerId; // 방장 아이디
    private String lastMessage; // 최근 메시지
    private ChatRoomType roomType;
    private int unreadCount;
    private List<String> participants; // 참여자 정보 리스트
    private LocalDateTime createdAt;

}

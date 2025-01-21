package com.one.social_project.domain.chat.dto;

import com.one.social_project.domain.chat.constant.ChatRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatParticipantsDTO {
    private String roomId; // 채팅방 아이디
    private String userId; // 유저 아이디
    private ChatRole role;
}

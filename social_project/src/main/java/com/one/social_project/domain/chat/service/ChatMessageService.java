package com.one.social_project.domain.chat.service;

import com.one.social_project.domain.chat.dto.ChatMessageDTO;
import com.one.social_project.domain.chat.entity.ChatMessage;
import com.one.social_project.domain.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;

    public void saveMessage(ChatMessageDTO chatMessageDTO) {
        ChatMessage chatMessage = ChatMessage.builder()
                .roomId(chatMessageDTO.getRoomId())
                .sender(chatMessageDTO.getSenderId())
                .message(chatMessageDTO.getMessage())
                .createdAt(LocalDateTime.now())
                .build();

        chatMessageRepository.save(chatMessage);
    }


    // 채팅방별 채팅 기록 조회
    public List<ChatMessage> getMessagesByRoomId(String roomId){
        return chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
    }
}

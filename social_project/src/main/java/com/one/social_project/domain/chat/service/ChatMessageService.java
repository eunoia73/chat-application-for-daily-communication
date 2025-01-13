package com.one.social_project.domain.chat.service;

import com.one.social_project.domain.chat.entity.ChatMessage;
import com.one.social_project.domain.chat.entity.ChatRoom;
import com.one.social_project.domain.chat.repository.ChatMessageRepository;
import com.one.social_project.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;


    // 채팅 저장
    public void saveMessage(String roomId, String sender, String message){
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        ChatMessage chat = ChatMessage.builder()
                .sender(sender)
                .message(message)
                .chatRoom(chatRoom)
                .createdAt(LocalDateTime.now())
                .build();

        chatMessageRepository.save(chat);
    }

    // 채팅방별 채팅 기록 조회
    public List<ChatMessage> getMessagesByRoomId(String roomId){
        return chatMessageRepository.findByChatRoomRoomIdOrderByCreatedAtAsc(roomId);
    }
}

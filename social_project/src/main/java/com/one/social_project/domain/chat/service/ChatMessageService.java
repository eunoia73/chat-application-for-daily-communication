package com.one.social_project.domain.chat.service;

import com.one.social_project.domain.chat.entity.ChatMessage;
import com.one.social_project.domain.chat.repository.mongo.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;

    // 채팅 저장
    public String saveMessage(String roomId, String sender, String message) {
        ChatMessage chatMessage = ChatMessage.builder()
                .roomId(roomId)
                .sender(sender)
                .message(message)
                .readers(new ArrayList<>(List.of(sender)))
                .createdAt(LocalDateTime.now())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        return savedMessage.getId();
    }

    // 채팅방별 채팅 기록 조회
    public Page<ChatMessage> getMessages(String roomId, Pageable pageable) {
        return chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId, pageable);
    }

    public List<ChatMessage> getUnreadMessages(String roomId, String sender){
        return chatMessageRepository.findAllByRoomIdAndReadersNotContaining(roomId, sender);
    }

    public void markMessageAsRead(String messageId, String userId){
        ChatMessage chatMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다."));

        if(!chatMessage.getReaders().contains(userId)){
            chatMessage.getReaders().add(userId);
            chatMessageRepository.save(chatMessage);
        }
    }

}
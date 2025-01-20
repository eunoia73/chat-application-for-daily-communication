package com.one.social_project.domain.chat.service;

import com.one.social_project.domain.chat.dto.ReadReceiptDTO;
import com.one.social_project.domain.chat.entity.ChatMessage;
import com.one.social_project.domain.chat.repository.mongo.ChatMessageRepository;
import jakarta.persistence.EntityNotFoundException;
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
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRoomId(roomId);
        chatMessage.setMessage(message);
        chatMessage.setSender(sender);

        // 보낸 사용자를 읽은 사람 목록에 자동 추가
        List<String> readers = new ArrayList<>();
        readers.add(sender);
        chatMessage.setReaders(readers);
        chatMessage.setCreatedAt(LocalDateTime.now());

        // 메시지 저장
        chatMessageRepository.save(chatMessage);

        // 저장된 메시지의 ID 반환
        return chatMessage.getId();
    }

    // 채팅방별 채팅 기록 조회
    public Page<ChatMessage> getMessagesByRoomId(String roomId, Pageable pageable){
        return chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId, pageable);
    }
}
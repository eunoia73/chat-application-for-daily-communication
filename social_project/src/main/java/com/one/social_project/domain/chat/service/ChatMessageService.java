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

    // 메시지를 읽은 사용자 추가
    @Transactional
    public void markAsRead(String messageId, String userId){
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("메시지를 찾을 수 없습니다. ID : "+ messageId));

        // 읽음 상태 업데이트
        if (!message.getReaders().contains(userId)) {
            message.getReaders().add(userId);
            chatMessageRepository.save(message);
        } else {
            System.out.println("이미 읽은 사용자입니다: messageId=" + messageId + ", userId=" + userId);
        }
    }

    // 메시지를 읽은 사용자 목록 조회
    @Transactional(readOnly = true)
    public List<String> getReadBy(String messageId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("메시지를 찾을 수 없습니다. ID: " + messageId));

        // 읽은 사용자 목록 반환
        return message.getReaders();
    }

    // 메시지를 읽은 사용자 목록 DTO 변환
    public ReadReceiptDTO getReadStatus(String messageId){
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("메시지를 찾을 수 없습니다. ID : "+ messageId));

        return new ReadReceiptDTO(
                message.getRoomId(),
                message.getMessage(),
                message.getSender(),
                message.getReaders()
        );
    }
}
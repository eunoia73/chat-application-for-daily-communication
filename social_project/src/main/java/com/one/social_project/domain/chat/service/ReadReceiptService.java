package com.one.social_project.domain.chat.service;

import com.one.social_project.domain.chat.dto.ReadReceiptDTO;
import com.one.social_project.domain.chat.entity.ChatMessage;
import com.one.social_project.domain.chat.entity.ReadReceipt;
import com.one.social_project.domain.chat.repository.ReadReceiptRepository;
import com.one.social_project.domain.chat.repository.mongo.ChatMessageRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReadReceiptService {

    private final ChatMessageRepository chatMessageRepository;
    private final ReadReceiptRepository readReceiptRepository;

    // 메시지 읽음 상태 업데이트
    @Transactional
    public void markAsRead(String messageId, String userId){
        // ReadReceipt 사용자 추가
        if(!readReceiptRepository.existsByMessageIdAndUserId(messageId, userId)){
            ReadReceipt readReceipt = ReadReceipt.builder()
                    .messageId(messageId)
                    .userId(userId)
                    .readAt(LocalDateTime.now())
                    .build();
            readReceiptRepository.save(readReceipt);
        }

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
                message.getReaders(),
                message.getCreatedAt()
        );
    }
}

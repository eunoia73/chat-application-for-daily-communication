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
        if (!readReceiptRepository.existsByMessageIdAndUserId(messageId, userId)) {
            saveReadReceipt(messageId, userId);
        }

        // 메시지 읽음 목록에 사용자 추가
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("메시지를 찾을 수 없습니다. ID: " + messageId));

        if (message.getReaders().add(userId)) { // 읽음 목록에 추가되었을 경우에만 저장
            chatMessageRepository.save(message);
        } else {
            System.out.println("이미 읽은 사용자입니다: messageId=" + messageId + ", userId=" + userId);
        }
    }

    private void saveReadReceipt(String messageId, String userId) {
        ReadReceipt readReceipt = ReadReceipt.builder()
                .messageId(messageId)
                .userId(userId)
                .readAt(LocalDateTime.now())
                .build();
        readReceiptRepository.save(readReceipt);
    }

    // 메시지를 읽은 사용자 목록 조회
    @Transactional(readOnly = true)
    public List<String> getReadBy(String messageId) {
        return chatMessageRepository.findById(messageId)
                .map(ChatMessage::getReaders)
                .orElseThrow(() -> new EntityNotFoundException("메시지를 찾을 수 없습니다. ID: " + messageId));
    }

    // 특정 채팅방에서 지정된 사용자가 읽지 않은 메시지의 개수를 계산
    @Transactional(readOnly = true)
    public int countUnreadMessages(String roomId, String userId) {
        // 읽지 않은 메시지 필터링 및 카운팅
        return (int) chatMessageRepository.findAllByRoomId(roomId).stream()
                .filter(message -> !readReceiptRepository.existsByMessageIdAndUserId(message.getId(), userId))
                .count();
    }

    // 메시지를 읽은 사용자 목록 DTO 변환
    @Transactional(readOnly = true)
    public ReadReceiptDTO getReadStatus(String messageId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("메시지를 찾을 수 없습니다. ID: " + messageId));

        return ReadReceiptDTO.builder()
                .roomId(message.getRoomId())
                .message(message.getMessage())
                .sender(message.getSender())
                .readers(message.getReaders())
                .readAt(message.getCreatedAt())
                .build();
    }
}

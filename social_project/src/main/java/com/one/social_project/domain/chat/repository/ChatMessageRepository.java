package com.one.social_project.domain.chat.repository;

import com.one.social_project.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 특정 채팅방에 채팅 조회
    List<ChatMessage> findByChatRoomRoomIdOrderByCreatedAtAsc(String roomId);

    // 특정 채팅방의 가장 최근 메시지 조회
    ChatMessage findFirstByChatRoomRoomIdOrderByCreatedAtDesc(String roomId);
}

package com.one.social_project.domain.chat.repository;

import com.one.social_project.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 특정 채팅방에 채팅 조회
    List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(String roomId);
}

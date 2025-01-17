package com.one.social_project.domain.chat.repository.mongo;

import com.one.social_project.domain.chat.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    // 특정 채팅방에 채팅 조회 (생성 시간 기준 오름차순 정렬)
    List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(String roomId);

    // 특정 채팅방의 가장 최근 메시지 조회
    ChatMessage findFirstByRoomIdOrderByCreatedAtDesc(String roomId);

    // 특정 채팅방의 모든 메시지 삭제
    void deleteAllByRoomId(String roomId);
}

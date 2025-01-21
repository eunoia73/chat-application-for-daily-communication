package com.one.social_project.domain.chat.repository;

import com.one.social_project.domain.chat.entity.ChatParticipants;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatParticipantsRepository extends JpaRepository<ChatParticipants, Long> {

    // 특정 채팅방에 속한 모든 참여자 목록을 조회하는 메서드
    List<ChatParticipants> findByChatRoomRoomId(String roomId);

    // 특정 채팅방과 특정 사용자(userId)에 해당하는 참여자를 조회하는 메서드
    Optional<ChatParticipants> findByChatRoomRoomIdAndUserId(String roomId, String userId);
}

package com.one.social_project.domain.chat.repository;

import com.one.social_project.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 채팅방 조회
    Optional<ChatRoom> findByRoomId(String roomId);

}

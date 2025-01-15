package com.one.social_project.domain.chat.repository;

import com.one.social_project.domain.chat.entity.ChatParticipants;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatParticipantsRepository extends JpaRepository<ChatParticipants, Long> {
    List<ChatParticipants> findByChatRoomRoomId(String roomId);
}

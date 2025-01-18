package com.one.social_project.domain.chat.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.one.social_project.domain.chat.constant.ChatRoomType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false, unique = true)
    private String roomId; // 채팅방 고유 ID

    @Column(name = "room_name", nullable = false)
    private String roomName; // 채팅방 이름

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false)
    private ChatRoomType roomType; // 채팅방 유형 (DM 또는 GM)

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // 채팅방 생성 시간

    // 참여자 관리
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatParticipants> participants;
}

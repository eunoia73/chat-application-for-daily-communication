package com.one.social_project.domain.chat.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.one.social_project.domain.chat.constant.ChatRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "participants")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatParticipants {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chat_room_id", nullable = false)
    @JsonBackReference // 자식 역할의 필드에 붙입니다.
    private ChatRoom chatRoom;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private ChatRole chatRole; // 사용자 역할(Owner 또는 Member)


}

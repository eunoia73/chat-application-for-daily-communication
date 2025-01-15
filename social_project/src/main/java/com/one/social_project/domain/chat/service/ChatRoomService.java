package com.one.social_project.domain.chat.service;

import com.one.social_project.domain.chat.dto.ChatRoomDTO;
import com.one.social_project.domain.chat.dto.ChatRoomType;
import com.one.social_project.domain.chat.entity.ChatMessage;
import com.one.social_project.domain.chat.entity.ChatRoom;
import com.one.social_project.domain.chat.repository.ChatMessageRepository;
import com.one.social_project.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    // 채팅방 생성 (개인 채팅방 또는 그룹 채팅방)
    public ChatRoomDTO createChatRoom(List<String> participants) {
        if (participants.size() == 2) {
            // 개인 채팅방 생성
            return createDirectChatRoom(participants.get(0), participants.get(1));
        } else if (participants.size() > 2) {
            // 그룹 채팅방 생성
            return createGroupChatRoom(participants);
        } else {
            throw new IllegalArgumentException("참여자가 2명 이상이어야 합니다.");
        }
    }

    // 특정 채팅방 조회
    public ChatRoomDTO getChatRoom(String roomId) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        // 최근 메시지 조회
        ChatMessage lastMessage = chatMessageRepository.findFirstByChatRoomRoomIdOrderByCreatedAtDesc(roomId);

        return ChatRoomDTO.builder()
                .roomId(chatRoom.getRoomId())
                .roomName(chatRoom.getRoomName())
                .createdAt(chatRoom.getCreatedAt())
                .lastMessage(lastMessage != null ? lastMessage.getMessage() : null)
                .build();
    }

    // 모든 채팅방 조회
    public List<ChatRoomDTO> getAllChatRooms() {
        List<ChatRoom> chatRooms = chatRoomRepository.findAll();

        return chatRooms.stream().map(chatRoom -> {
            ChatMessage lastMessage = chatMessageRepository.findFirstByChatRoomRoomIdOrderByCreatedAtDesc(chatRoom.getRoomId());

            return ChatRoomDTO.builder()
                    .roomId(chatRoom.getRoomId())
                    .roomName(chatRoom.getRoomName())
                    .createdAt(chatRoom.getCreatedAt())
                    .lastMessage(lastMessage != null ? lastMessage.getMessage() : null)
                    .build();
        }).collect(Collectors.toList());
    }

    // 채팅방 삭제(추후 삭제 -> 나가기)
    public String deleteChatRoom(String roomId) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));
        chatRoomRepository.delete(chatRoom);
        return "채팅방 삭제 완료!";
    }

    // 개인 채팅방 생성
    private ChatRoomDTO createDirectChatRoom(String user1, String user2) {
        String roomId = generatePersonalRoomId(user1, user2);

        // 기존 채팅방이 있는지 확인
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByRoomId(roomId);
        if (existingRoom.isPresent()) {
            return convertToDTO(existingRoom.get());
        }

        // 새 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .roomId(roomId)
                .roomName(user1 + " & " + user2)
                .roomType(ChatRoomType.DM)
                .createdAt(LocalDateTime.now())
                .build();

        chatRoomRepository.save(chatRoom);
        return convertToDTO(chatRoom);
    }

    // 그룹 채팅방 생성
    private ChatRoomDTO createGroupChatRoom(List<String> participants) {
        String roomId = UUID.randomUUID().toString();

        // 사용자 이름을 정렬 후 이름 조합 생성
        String roomName = participants.stream()
                .sorted()
                .collect(Collectors.joining(", "));

        // 새 그룹 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .roomId(roomId)
                .roomName(roomName)
                .roomType(ChatRoomType.GM)
                .createdAt(LocalDateTime.now())
                .build();

        chatRoomRepository.save(chatRoom);
        return convertToDTO(chatRoom);
    }

    // 두 사용자 ID를 정렬하여 고유한 roomId 생성
    private String generatePersonalRoomId(String user1, String user2) {
        return (user1.compareTo(user2) < 0) ? user1 + ":" + user2 : user2 + ":" + user1;
    }

    // entity -> dto
    private ChatRoomDTO convertToDTO(ChatRoom chatRoom) {
        return ChatRoomDTO.builder()
                .roomId(chatRoom.getRoomId())
                .roomName(chatRoom.getRoomName())
                .roomType(chatRoom.getRoomType())
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }
}

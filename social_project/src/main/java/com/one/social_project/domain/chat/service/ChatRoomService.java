package com.one.social_project.domain.chat.service;

import com.one.social_project.domain.chat.dto.ChatRoomDTO;
import com.one.social_project.domain.chat.entity.ChatRoom;
import com.one.social_project.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;

    // 채팅방 생성
    public ChatRoomDTO createChatRoom(ChatRoomDTO chatRoomDTO) {
        String roomId = UUID.randomUUID().toString(); // 랜덤한 채팅방 ID 생성
        chatRoomDTO.setRoomId(roomId);

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setRoomId(roomId);
        chatRoom.setRoomName(chatRoomDTO.getRoomName());
        chatRoom = chatRoomRepository.save(chatRoom);

        return ChatRoomDTO.builder()
                .roomId(chatRoom.getRoomId())
                .roomName(chatRoomDTO.getRoomName())
                .createdAt(LocalDateTime.now().toString())
                .build();
    }

    // 단일 채팅방 조회
    public ChatRoomDTO getChatRoom(String roomId) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다.")) ;

        return ChatRoomDTO.builder()
                .roomId(chatRoom.getRoomId())
                .roomName(chatRoom.getRoomName())
                .createdAt(LocalDateTime.now().toString())
                .build();
    }

    // 전체 채팅방 조회
    public List<ChatRoomDTO> getAllChatRooms() {
        List<ChatRoom> chatRooms = chatRoomRepository.findAll();
        return chatRooms.stream()
                .map(chatRoom -> ChatRoomDTO.builder()
                        .roomId(chatRoom.getRoomId())
                        .roomName(chatRoom.getRoomName())
                        .createdAt(LocalDateTime.now().toString())
                        .build()
                )
                .toList();
    }

    // 채팅방 삭제(추후 삭제 -> 나가기 변경)
    public String deleteChatRoom(String roomId) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));
        chatRoomRepository.delete(chatRoom);
        return "채팅방 삭제 완료!";
    }

}

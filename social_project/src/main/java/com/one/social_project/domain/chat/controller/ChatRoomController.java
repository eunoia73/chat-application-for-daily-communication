package com.one.social_project.domain.chat.controller;

import com.one.social_project.domain.chat.dto.ChatParticipantsDTO;
import com.one.social_project.domain.chat.dto.ChatRoomDTO;
import com.one.social_project.domain.chat.constant.ChatRoomType;
import com.one.social_project.domain.chat.entity.ChatMessage;
import com.one.social_project.domain.chat.service.ChatMessageService;
import com.one.social_project.domain.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    // 채팅방 생성 (개인 채팅방 또는 그룹 채팅방)
    @PostMapping("/createroom")
    public ResponseEntity<ChatRoomDTO> createChatRoom(@RequestBody ChatRoomDTO chatRoomDTO) {
        try {
            ChatRoomDTO createdRoom;

            // 채팅방 유형에 따른 처리
            if (chatRoomDTO.getRoomType() == ChatRoomType.DM) {
                // 개인 채팅방 생성 : 참여자 2명만 허용
                if (chatRoomDTO.getParticipants() == null || chatRoomDTO.getParticipants().size() != 2) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "개인 채팅 참여자는 2명이어야 합니다.");
                }
                createdRoom = chatRoomService.createChatRoom(chatRoomDTO.getParticipants());
            } else if (chatRoomDTO.getRoomType() == ChatRoomType.GM) {
                // 그룹 채팅방 생성 : 참여자 3명 이상 필요
                if (chatRoomDTO.getParticipants() == null || chatRoomDTO.getParticipants().size() < 3) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "그룹 채팅 참여자는 최소 3명 이상이어야 합니다.");
                }
                createdRoom = chatRoomService.createChatRoom(chatRoomDTO.getParticipants());
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 방 유형 입니다: " + chatRoomDTO.getRoomType());
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "채팅방을 생성 중 오류 발생", e);
        }
    }

    // 특정 채팅방 조회
    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoomDTO> getChatRoom(@PathVariable("roomId") String roomId) {
        ChatRoomDTO chatRoom = chatRoomService.getChatRoom(roomId);
        return ResponseEntity.ok(chatRoom);
    }

    // 모든 채팅방 조회
    @GetMapping("/roomlist")
    public ResponseEntity<List<ChatRoomDTO>> getAllChatRooms() {
        List<ChatRoomDTO> chatRooms = chatRoomService.getAllChatRooms();
        return ResponseEntity.ok(chatRooms);
    }

    // 특정 채팅방의 메시지 조회
    @GetMapping("/{roomId}/chat-list")
    public ResponseEntity<Page<ChatMessage>> getMessages(@PathVariable("roomId") String roomId , Pageable pageable) {
        System.out.println("Room ID: " + roomId); // 디버깅용 출력
        Page<ChatMessage> messages = chatMessageService.getMessages(roomId , pageable);
        if (messages.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.ok(messages);
    }

    // 특정 채팅방의 참여자 상세 조회
    @GetMapping("/{roomId}/participants")
    public ResponseEntity<List<ChatParticipantsDTO>> getChatRoomParticipants(@PathVariable String roomId){
        List<ChatParticipantsDTO> participants = chatRoomService.getChatRoomParticipants(roomId);
        return ResponseEntity.ok(participants);
    }

    // 사용자별 채팅방 목록 조회
    @GetMapping("/rooms/{userId}")
    public ResponseEntity<List<ChatRoomDTO>> getUserChatRooms(@PathVariable String userId){
        List<ChatRoomDTO> chatRooms = chatRoomService.getUserChatRooms(userId);
        return ResponseEntity.ok(chatRooms);
    }

    // 채팅방 나가기 및 채팅방 참여자 없을 시 채팅방 삭제
    @DeleteMapping("/{roomId}/leave/{participantId}")
    public ResponseEntity<String> leaveChatRoom(@PathVariable("roomId") String roomId, @PathVariable("participantId") String userId) {
        try {
            String result = chatRoomService.leaveChatRoom(roomId, userId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("채팅방을 찾을 수 없습니다.");
        }
    }
}

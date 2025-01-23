package com.one.social_project.domain.chat.controller;

import com.one.social_project.domain.chat.dto.ChatParticipantsDTO;
import com.one.social_project.domain.chat.dto.ChatRoomDTO;
import com.one.social_project.domain.chat.constant.ChatRoomType;
import com.one.social_project.domain.chat.entity.ChatMessage;
import com.one.social_project.domain.chat.service.ChatMessageService;
import com.one.social_project.domain.chat.service.ChatRoomService;
import com.one.social_project.domain.notifications.service.NotificationService;
import com.one.social_project.domain.user.util.TokenProvider;
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

    private final TokenProvider tokenProvider;
    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final NotificationService notificationService;

    // 채팅방 생성 (개인 채팅방 또는 그룹 채팅방)
    @PostMapping("/room")
    public ResponseEntity<ChatRoomDTO> createChatRoom(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ChatRoomDTO chatRoomDTO) {

        try {
            String token = authHeader.substring(7);
            String createrNickname = tokenProvider.getNicknameFromToken(token);

            validateChatRoomRequest(chatRoomDTO, createrNickname);

            ChatRoomDTO createdRoom = chatRoomService.createChatRoom(
                    token, chatRoomDTO.getRoomName(), chatRoomDTO.getParticipants());

            //채팅방 정보로 알림 생성 및 저장
            notificationService.generateNotifications(createdRoom);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "채팅방을 생성 중 오류 발생", e);
        }
    }

    // 특정 채팅방의 메시지 조회
    @GetMapping("/{roomId}/chat-list")
    public ResponseEntity<Page<ChatMessage>> getMessages(@PathVariable("roomId") String roomId , Pageable pageable) {
        log.info("Room Id: {}" , roomId);
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
    @GetMapping("/roomlist")
    public ResponseEntity<List<ChatRoomDTO>> getUserChatRooms(@RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);

        List<ChatRoomDTO> chatRooms = chatRoomService.getUserChatRooms(token);

        return ResponseEntity.ok(chatRooms);
    }

    // 채팅방 나가기 및 채팅방 참여자 없을 시 채팅방 삭제
    @DeleteMapping("/{roomId}/leave/{participantId}")
    public ResponseEntity<String> leaveChatRoom(
                @PathVariable("roomId") String roomId,
                @PathVariable("participantId") String nickName) {
            boolean isDeleted = chatRoomService.leaveChatRoom(roomId, nickName);

            if(isDeleted){
                return ResponseEntity.ok("채팅방이 삭제되었습니다.");
            }else{
                return ResponseEntity.ok(nickName + "님이 채팅방에서 나갔습니다.");
            }
    }

    private void validateChatRoomRequest(ChatRoomDTO chatRoomDTO, String createdNickname){
        if(!chatRoomDTO.getParticipants().contains(createdNickname)){
            chatRoomDTO.getParticipants().add(createdNickname);
        }

        // 채팅방 유형에 따른 검증
        if (chatRoomDTO.getRoomType() == ChatRoomType.DM) {
            if (chatRoomDTO.getParticipants() == null || chatRoomDTO.getParticipants().size() != 2) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "개인 채팅 참여자는 2명이어야 합니다.");
            }
        } else if (chatRoomDTO.getRoomType() == ChatRoomType.GM) {
            if (chatRoomDTO.getParticipants() == null || chatRoomDTO.getParticipants().size() < 3) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "그룹 채팅 참여자는 최소 3명 이상이어야 합니다.");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 방 유형입니다: " + chatRoomDTO.getRoomType());
        }
    }
}

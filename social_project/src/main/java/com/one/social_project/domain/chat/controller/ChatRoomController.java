package com.one.social_project.domain.chat.controller;

import com.one.social_project.domain.chat.dto.ChatRoomDTO;
import com.one.social_project.domain.chat.entity.ChatMessage;
import com.one.social_project.domain.chat.service.ChatMessageService;
import com.one.social_project.domain.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    // 채팅방 생성
    @PostMapping("/createroom")
    public ResponseEntity<ChatRoomDTO> createChatRoom(@RequestBody ChatRoomDTO chatRoomDTO) {
        ChatRoomDTO createRoom = chatRoomService.createChatRoom(chatRoomDTO);
        return ResponseEntity.ok(createRoom);
    }

    // 단일 채팅방 조회
    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoomDTO> getChatRoom(@PathVariable("roomId") String roomId) {
            ChatRoomDTO chatRoom = chatRoomService.getChatRoom(roomId);
            return ResponseEntity.ok(chatRoom);
    }

    // 전체 채팅방 조회
    @GetMapping("/roomlist")
    public ResponseEntity<List<ChatRoomDTO>> getAllChatRooms() {
        List<ChatRoomDTO> chatRooms = chatRoomService.getAllChatRooms();
        return ResponseEntity.ok(chatRooms);
    }


    // 특정 채팅방의 채팅 조회
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<ChatMessage>> getMessages(@PathVariable("roomId") String roomId) {
        System.out.println("Room ID: " + roomId); // 디버깅용 출력
        List<ChatMessage> messages = chatMessageService.getMessagesByRoomId(roomId);
        if (messages.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.ok(messages);
    }

    // 채팅방 삭제(추후 삭제 -> 나가기 변경)
    @DeleteMapping("/{roomId}/delete")
    public ResponseEntity<String> deleteChatRoom(@PathVariable("roomId") String roomId) {
        try{
            String result = chatRoomService.deleteChatRoom(roomId);
            return ResponseEntity.ok(result);
        }catch (RuntimeException e){
            return ResponseEntity.status(404).body("채팅방을 찾을 수 없습니다.");
        }
    }
}

package com.one.social_project.domain.chat.controller;


import com.one.social_project.domain.chat.dto.ChatMessageDTO;
import com.one.social_project.domain.chat.entity.ChatMessage;
import com.one.social_project.domain.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatMessageController {

    private final SimpMessageSendingOperations template;
    private final ChatMessageService chatMessageService;

    // 메시지 전송
    @MessageMapping("/message")
    public void sendMessage(@Payload ChatMessageDTO chat) {
        log.info("CHAT {}", chat);

        chatMessageService.saveMessage(chat);
        template.convertAndSend("/sub/chat/room/" + chat.getRoomId(), chat);
    }

    // 특정 채팅방의 메시지 조회
    @GetMapping("/{roomId}/chat-list")
    public ResponseEntity<List<ChatMessage>> chatlist(@PathVariable String roomId){
        try{
            List<ChatMessage> messages = chatMessageService.getMessagesByRoomId(roomId);
            if(messages.isEmpty()){
                return ResponseEntity.status(204).build();
            }
            return ResponseEntity.ok(messages);
        }catch (RuntimeException e){
            return ResponseEntity.status(404).build();
        }

    }
}

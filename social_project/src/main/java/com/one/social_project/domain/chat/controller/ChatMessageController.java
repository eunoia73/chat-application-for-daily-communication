package com.one.social_project.domain.chat.controller;

import com.one.social_project.domain.chat.dto.ReadReceiptDTO;
import com.one.social_project.domain.chat.service.ChatMessageService;
import com.one.social_project.domain.chat.service.ReadReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final ReadReceiptService readReceiptService;

    @GetMapping("/{roomId}/messages/{messageId}/read-status")
    public ResponseEntity<ReadReceiptDTO> getReadBy(@PathVariable String messageId) {
        ReadReceiptDTO readStatus = readReceiptService.getReadStatus(messageId);
        return ResponseEntity.ok(readStatus);
    }
}

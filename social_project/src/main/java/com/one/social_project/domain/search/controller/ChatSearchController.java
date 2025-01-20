package com.one.social_project.domain.search.controller;

import com.one.social_project.domain.chat.entity.ChatMessage;
import com.one.social_project.domain.search.ChatSearchCondition;
import com.one.social_project.domain.search.service.ChatSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatSearchController {

    private final ChatSearchService chatSearchService;

    @GetMapping("/api/chat/{roomId}/chat-list/search")
    public List<ChatMessage> searchByMessageAndDateRange(
            @PathVariable("roomId") String roomId, // roomId를 경로 변수로 받음
            ChatSearchCondition condition) {
        return chatSearchService.searchByMessageAndDateRange(roomId, condition);
    }


}


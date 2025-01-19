package com.one.social_project.domain.search.controller;

import com.one.social_project.domain.chat.entity.ChatMessage;
import com.one.social_project.domain.chat.repository.mongo.ChatMessageRepository;
import com.one.social_project.domain.search.ChatSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatSearchController {

    private final ChatMessageRepository chatMessageRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @GetMapping("/api/chatMessage/search")
    public List<ChatMessage> searchMemberV2(ChatSearchCondition condition) {

        return chatMessageRepository.searchByMessageAndDateRange(condition);
    }


}


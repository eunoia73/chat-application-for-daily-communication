package com.one.social_project.domain.search.service;

import com.one.social_project.domain.chat.entity.ChatMessage;
import com.one.social_project.domain.chat.repository.mongo.ChatMessageRepository;
import com.one.social_project.domain.search.ChatSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatSearchService {
    private final ChatMessageRepository chatMessageRepository;

    public List<ChatMessage> searchByMessageAndDateRange(String roomId, ChatSearchCondition condition) {
        return chatMessageRepository.searchByMessageAndDateRange(roomId, condition);
    }


}

package com.one.social_project.domain.search.repository;


import com.one.social_project.domain.chat.entity.ChatMessage;
import com.one.social_project.domain.search.ChatSearchCondition;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepositoryCustom {
    List<ChatMessage> searchByMessageAndDateRange(ChatSearchCondition chatSearchCondition);

}

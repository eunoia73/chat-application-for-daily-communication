package com.one.social_project.domain.search.service;

import com.one.social_project.domain.chat.entity.ChatMessage;
import com.one.social_project.domain.chat.repository.mongo.ChatMessageRepository;
import com.one.social_project.domain.search.ChatSearchCondition;
import com.one.social_project.domain.search.UserSearchCondition;
import com.one.social_project.domain.search.dto.UserSearchDTO;
import com.one.social_project.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    //mongodb 메시지로 chatMessage 조회
    public List<ChatMessage> searchByMessageAndDateRange(String roomId, ChatSearchCondition condition) {
        return chatMessageRepository.searchByMessageAndDateRange(roomId, condition);
    }

    //nickname으로 user조회
    public Page<UserSearchDTO> searchUserByNickname(UserSearchCondition condition, Pageable pageable){
        return userRepository.searchUserByNickname(condition, pageable);
    }

}

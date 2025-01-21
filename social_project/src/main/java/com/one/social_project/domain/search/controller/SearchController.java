package com.one.social_project.domain.search.controller;

import com.one.social_project.domain.chat.entity.ChatMessage;
import com.one.social_project.domain.search.ChatSearchCondition;
import com.one.social_project.domain.search.UserSearchCondition;
import com.one.social_project.domain.search.dto.UserSearchDTO;
import com.one.social_project.domain.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SearchController {

    private final SearchService chatSearchService;

    //message로 chat조회
    @GetMapping("/api/chat/{roomId}/chat-list/search")
    public List<ChatMessage> searchByMessageAndDateRange(
            @PathVariable("roomId") String roomId, // roomId를 경로 변수로 받음
            ChatSearchCondition condition) {
        return chatSearchService.searchByMessageAndDateRange(roomId, condition);
    }

    //nickname으로 user 검색
    @GetMapping("/api/users/search")
    public Page<UserSearchDTO> userSearchByNickname(UserSearchCondition condition, Pageable pageable) {
        return chatSearchService.searchUserByNickname(condition, pageable);
    }


}


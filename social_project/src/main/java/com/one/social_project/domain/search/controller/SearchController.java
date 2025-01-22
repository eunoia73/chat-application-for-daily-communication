package com.one.social_project.domain.search.controller;

import com.one.social_project.domain.chat.entity.ChatMessage;
import com.one.social_project.domain.search.ChatSearchCondition;
import com.one.social_project.domain.search.PageableResponse;
import com.one.social_project.domain.search.UserSearchCondition;
import com.one.social_project.domain.search.dto.UserSearchDTO;
import com.one.social_project.domain.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Map<String, Object>> userSearchByNickname(UserSearchCondition condition, Pageable pageable) {

        Page<UserSearchDTO> result = chatSearchService.searchUserByNickname(condition, pageable);

        // PageableResponse 객체 생성
        PageableResponse pageableResponse = new PageableResponse(
                result.getNumber(),
                result.getSize(),
                pageable.getOffset(),
                result.isLast()
        );

        // Map으로 응답 구성
        Map<String, Object> response = new HashMap<>();
        response.put("content", result.getContent());
        response.put("pageable", pageableResponse);

        return ResponseEntity.ok(response);
    }


}


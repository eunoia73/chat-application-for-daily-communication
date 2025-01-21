package com.one.social_project.domain.search.repository;

import com.one.social_project.domain.chat.entity.ChatMessage;
import com.one.social_project.domain.search.ChatSearchCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
class ChatMessageRepositoryImplTest {

    @Autowired
    private MongoOperations mongoOperations;

    private ChatMessageRepositoryImpl chatMessageRepository;

    @BeforeEach
    void setUp() {
        chatMessageRepository = new ChatMessageRepositoryImpl(mongoOperations);

        // 테스트 데이터 초기화
        mongoOperations.dropCollection(ChatMessage.class);

        ChatMessage message1 = ChatMessage.builder()
                .message("Hello world")
                .sender("user1")
                .roomId("room1")
                .createdAt(LocalDateTime.of(2023, 1, 1, 10, 0))
                .build();

        ChatMessage message2 = ChatMessage.builder()
                .message("Test message")
                .sender("user2")
                .roomId("room1")
                .createdAt(LocalDateTime.of(2023, 1, 2, 15, 30))
                .build();

        ChatMessage message3 = ChatMessage.builder()
                .message("Hello Spring")
                .sender("user3")
                .roomId("room1")
                .createdAt(LocalDateTime.of(2023, 1, 3, 20, 0))
                .build();

        mongoOperations.save(message1);
        mongoOperations.save(message2);
        mongoOperations.save(message3);
    }

    @Test
    @DisplayName("메시지와 날짜 범위로 검색")
    void testSearchByMessageAndDateRange() {
        // Given
        String roomId = "room1";
        ChatSearchCondition condition = new ChatSearchCondition();
        condition.setMessage("Hello");
        condition.setCreatedAtGoe("20230101");
        condition.setCreatedAtLoe("20230103");

        // When
        List<ChatMessage> results = chatMessageRepository.searchByMessageAndDateRange(roomId, condition);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(2); // "Hello world", "Hello Spring"
        assertThat(results.get(0).getMessage()).contains("Hello");
    }

    @Test
    @DisplayName("메시지만으로 검색")
    void testSearchByMessageOnly() {
        // Given
        String roomId = "room1";
        ChatSearchCondition condition = new ChatSearchCondition();
        condition.setMessage("Test");

        // When
        List<ChatMessage> results = chatMessageRepository.searchByMessageAndDateRange(roomId, condition);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getMessage()).isEqualTo("Test message");
    }

    @Test
    @DisplayName("날짜 범위만으로 검색")
    void testSearchByDateRangeOnly() {
        // Given
        String roomId = "room1";
        ChatSearchCondition condition = new ChatSearchCondition();
        condition.setCreatedAtGoe("20230102");
        condition.setCreatedAtLoe("20230103");

        // When
        List<ChatMessage> results = chatMessageRepository.searchByMessageAndDateRange(roomId, condition);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(2); // "Test message", "Hello Spring"
    }

    @Test
    @DisplayName("메시지와 날짜 범위(시작날짜)로 검색")
    void testSearchByMessageAndCreatedAtGoe() {
        // Given
        String roomId = "room1";
        ChatSearchCondition condition = new ChatSearchCondition();
        condition.setMessage("Hello");
        condition.setCreatedAtGoe("20230102");

        // When
        List<ChatMessage> results = chatMessageRepository.searchByMessageAndDateRange(roomId, condition);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(1); // "Hello Spring"
        assertThat(results.get(0).getMessage()).contains("Hello");
    }

    @Test
    @DisplayName("메시지와 날짜 범위(종료날짜)로 검색")
    void testSearchByMessageAndCreatedAtLoe() {
        // Given
        String roomId = "room1";
        ChatSearchCondition condition = new ChatSearchCondition();
        condition.setMessage("Hello");
        condition.setCreatedAtLoe("20230102");

        // When
        List<ChatMessage> results = chatMessageRepository.searchByMessageAndDateRange(roomId, condition);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(1); // "Hello Spring"
        assertThat(results.get(0).getMessage()).contains("Hello");
    }

    @Test
    @DisplayName("조건에 맞는 메시지가 없을 때")
    void testSearchNoResults() {
        // Given
        String roomId = "room1";
        ChatSearchCondition condition = new ChatSearchCondition();
        condition.setMessage("Non-existing");

        // When
        List<ChatMessage> results = chatMessageRepository.searchByMessageAndDateRange(roomId, condition);

        // Then
        assertThat(results).isEmpty();
    }
}
package com.one.social_project.domain.search.repository;//package com.one.social_project.domain.search.repository;

import com.one.social_project.domain.chat.entity.ChatMessage;
import com.one.social_project.domain.search.ChatSearchCondition;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
public class ChatMessageRepositoryImpl implements ChatMessageRepositoryCustom {

    private final MongoOperations mongoOperations;

    public ChatMessageRepositoryImpl(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public List<ChatMessage> searchByMessageAndDateRange(ChatSearchCondition chatSearchCondition) {

        // MongoTemplate을 사용하여 정규식 쿼리 작성
        Query query = new Query();
        if (chatSearchCondition.getMessage() != null && !chatSearchCondition.getMessage().isEmpty()) {
            query.addCriteria(Criteria.where("message").regex(".*" + chatSearchCondition.getMessage() + ".*", "i")); // 'i'는 대소문자 구분 없음
            System.out.println("/////"+chatSearchCondition.getCreatedAtLoe());
        }

//        if (chatSearchCondition.getCreatedAtGoe() != null && chatSearchCondition.getCreatedAtLoe() != null) {
//            LocalDateTime createdAtGoe = null;
//            LocalDateTime createdAtLoe = null;
//
//            if (chatSearchCondition.getCreatedAtGoe() != null) {
//                createdAtGoe = LocalDate.parse(chatSearchCondition.getCreatedAtGoe(), FORMATTER).atStartOfDay();
//                System.out.println("createdAtGoe" + createdAtGoe);
//            }
//            if (chatSearchCondition.getCreatedAtLoe() != null) {
//                createdAtLoe = LocalDate.parse(chatSearchCondition.getCreatedAtLoe(), FORMATTER).atStartOfDay();
//                System.out.println("createdAtLoe" + createdAtLoe);
//            }
//
//            query.addCriteria(Criteria.where("createdAt").gte(createdAtGoe).lte(createdAtLoe));
//            System.out.println(query);
//        }

        return mongoOperations.find(query, ChatMessage.class);
    }
}


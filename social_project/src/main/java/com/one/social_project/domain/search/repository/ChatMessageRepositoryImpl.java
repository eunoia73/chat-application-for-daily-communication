package com.one.social_project.domain.search.repository;

import com.one.social_project.domain.chat.entity.ChatMessage;
import com.one.social_project.domain.search.ChatSearchCondition;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
public class ChatMessageRepositoryImpl implements ChatMessageRepositoryCustom {

    private final MongoOperations mongoOperations;

    public ChatMessageRepositoryImpl(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }


    @Override
    public List<ChatMessage> searchByMessageAndDateRange(ChatSearchCondition chatSearchCondition) {


        // MongoTemplate을 사용하여 정규식 쿼리 작성
        Query query = new Query();
        if (chatSearchCondition.getMessage() != null && !chatSearchCondition.getMessage().isEmpty()) {
            query.addCriteria(Criteria.where("message").regex(".*" + chatSearchCondition.getMessage() + ".*", "i")); // 'i'는 대소문자 구분 없음
        }

        // yyMMdd' 형식으로 날짜 파싱
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        if (chatSearchCondition.getCreatedAtGoe() != null && chatSearchCondition.getCreatedAtLoe() != null) {

            LocalDateTime localDateTimeGoe = getLocalDateTimeGoe(chatSearchCondition, formatter);
            LocalDateTime localDateTimeLoe = getLocalDateTimeLoe(chatSearchCondition, formatter);

            Criteria dateRangeCriteria = Criteria.where("createdAt")
                    .gte(localDateTimeGoe)
                    .lte(localDateTimeLoe);

            query.addCriteria(dateRangeCriteria);

        }else if (chatSearchCondition.getCreatedAtGoe() != null && chatSearchCondition.getCreatedAtLoe() == null){
            LocalDateTime localDateTimeGoe = getLocalDateTimeGoe(chatSearchCondition, formatter);
            Criteria dateRangeCriteria = Criteria.where("createdAt")
                    .gte(localDateTimeGoe);
            query.addCriteria(dateRangeCriteria);

        } else if (chatSearchCondition.getCreatedAtGoe() == null && chatSearchCondition.getCreatedAtLoe() != null) {
            LocalDateTime localDateTimeLoe = getLocalDateTimeLoe(chatSearchCondition, formatter);
            Criteria dateRangeCriteria = Criteria.where("createdAt")
                    .lte(localDateTimeLoe);
            query.addCriteria(dateRangeCriteria);
        }


        return mongoOperations.find(query, ChatMessage.class);
    }

    private static LocalDateTime getLocalDateTimeGoe(ChatSearchCondition chatSearchCondition, DateTimeFormatter formatter) {
        String createdAtGoeString = chatSearchCondition.getCreatedAtGoe();
        LocalDate localDateGoe = LocalDate.parse(createdAtGoeString, formatter);
        // LocalDate를 LocalDateTime으로 변환 (시간은 기본적으로 00:00로 설정)
        LocalDateTime localDateTimeGoe = localDateGoe.atStartOfDay();
        return localDateTimeGoe;
    }

    private static LocalDateTime getLocalDateTimeLoe(ChatSearchCondition chatSearchCondition, DateTimeFormatter formatter) {
        String createdAtLoeString = chatSearchCondition.getCreatedAtLoe();
        LocalDate localDateLoe = LocalDate.parse(createdAtLoeString, formatter);
        LocalDateTime localDateTimeLoe = localDateLoe.atTime(LocalTime.MAX); // 23:59:59.999999로 설정
        return localDateTimeLoe;
    }

}




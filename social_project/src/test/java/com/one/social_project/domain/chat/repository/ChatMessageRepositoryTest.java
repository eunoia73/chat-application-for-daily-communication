//package com.one.social_project.domain.chat.repository;
//
//import com.one.social_project.domain.chat.entity.ChatMessage;
//import com.one.social_project.domain.chat.entity.QChatMessage;
//import com.one.social_project.domain.user.basic.entity.QUsers;
//import com.one.social_project.domain.user.basic.entity.Users;
//import com.querydsl.core.types.dsl.BooleanExpression;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//import jakarta.persistence.EntityManager;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static com.one.social_project.domain.chat.entity.QChatMessage.*;
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Transactional
//class ChatMessageRepositoryTest {
//
//    @Autowired
//    EntityManager em;
//
//    @Autowired
//    ChatMessageRepository chatMessageRepository;
//
//    JPAQueryFactory queryFactory;
//
//    @BeforeEach
//    public void before() {
//        queryFactory = new JPAQueryFactory(em);
//
//        ChatMessage message1 = new ChatMessage(null, "room1", "user1", "안녕하세요!", LocalDateTime.now());
//        ChatMessage message2 = new ChatMessage(null, "room1", "user1", "안녕!", LocalDateTime.now());
//        ChatMessage message3 = new ChatMessage(null, "room1", "user1", "hi!", LocalDateTime.now());
//        ChatMessage message4 = new ChatMessage(null, "room1", "user1", "안녕안녕!", LocalDateTime.now());
//
//        em.persist(message1);
//        em.persist(message2);
//        em.persist(message3);
//        em.persist(message4);
//    }
//
//    @Test
//    public void selectByParam() {
//        String content = "안녕";
//
//        List<ChatMessage> result = searchMessage(content);
//        Assertions.assertThat(result.size()).isEqualTo(3);
//
//    }
//
//    private List<ChatMessage> searchMessage(String content) {
//        return queryFactory
//                .selectFrom(chatMessage)
//                .where(contentLike(content))
//                .fetch();
//    }
//
//    private BooleanExpression contentLike(String content) {
//        if (content == null) {
//            return null;
//        }
//        return chatMessage.message.like("%" + content + "%");
//    }
//
//
//}
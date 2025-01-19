//package com.one.social_project.domain.search.repository;
//
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
//import java.util.List;
//
//import static com.one.social_project.domain.user.basic.entity.QUsers.*;
//
//@SpringBootTest
//@Transactional
//class UserRepositoryImplTest {
//
//    @Autowired
//    EntityManager em;
//
//    JPAQueryFactory queryFactory;
//
//
//    @BeforeEach
//    public void before() {
//        queryFactory = new JPAQueryFactory(em);
//
//        Users user1 = new Users("example1@example.com", "1234", "u1", true, "MEMBER", true);
//        Users user2 = new Users("example2@example.com", "1234", "u2", true, "MEMBER", true);
//        Users user3 = new Users("example3@example.com", "1234", "u3", true, "MEMBER", true);
//        Users user4 = new Users("example4@example.com", "1234", "u4", true, "MEMBER", true);
//
//        em.persist(user1);
//        em.persist(user2);
//        em.persist(user3);
//        em.persist(user4);
//    }
//
//    @Test
//    public void selectByParam() {
//        String nickname = "u2";
//
//        List<Users> result = searchUser(nickname);
//        Assertions.assertThat(result.size()).isEqualTo(1);
//
//    }
//
//    private List<Users> searchUser(String nickname) {
//        QUsers users = QUsers.users;
//        return queryFactory
//                .selectFrom(users)
//                .where(userNicknameLike(nickname))
//                .fetch();
//    }
//
//    private BooleanExpression userNicknameLike(String userNicknameCond) {
//        if (userNicknameCond == null) {
//            return null;
//        }
//        return users.nickname.like("%" + userNicknameCond + "%");
//    }
//
//
//}
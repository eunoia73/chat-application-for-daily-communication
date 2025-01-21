package com.one.social_project.domain.search.repository;

import com.one.social_project.domain.search.dto.QUserSearchDTO;
import com.one.social_project.domain.search.dto.UserSearchDTO;
import com.one.social_project.domain.user.entity.User;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.one.social_project.domain.user.entity.QUser.*;


@SpringBootTest
@Transactional
class UserRepositoryImplTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;


    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);

        User user1 = new User("example1@example.com", "1234", "u1", true, "MEMBER", true);
        User user2 = new User("example2@example.com", "1234", "u2", true, "MEMBER", true);
        User user3 = new User("example3@example.com", "1234", "u3", true, "MEMBER", true);
        User user4 = new User("example4@example.com", "1234", "u4", true, "MEMBER", true);

        em.persist(user1);
        em.persist(user2);
        em.persist(user3);
        em.persist(user4);
    }


    @Test
    public void selectByParam() {
        String nickname = "u";
        // 페이지 번호와 크기를 지정하여 Pageable 생성
        Pageable pageable = PageRequest.of(0, 4);

        // searchUser 메서드 호출
        Page<UserSearchDTO> result = searchUser(nickname, pageable);

        Assertions.assertThat(result.getSize()).isEqualTo(4);
        System.out.println("result" + result.get()); // 결과 출력

    }

    private Page<UserSearchDTO> searchUser(String nickname, Pageable pageable) {
//        QUser users = user;
        QueryResults<UserSearchDTO> result = queryFactory
                .select(new QUserSearchDTO(
                        user.id.as("userId"),
                        user.nickname,
                        user.email,
                        user.profileImg
                ))
                .from(user)
                .where(userNicknameLike(nickname))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<UserSearchDTO> content = result.getResults();
        long total = result.getTotal();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression userNicknameLike(String userNicknameCond) {
        if (userNicknameCond == null) {
            return null;
        }
        return user.nickname.like("%" + userNicknameCond + "%");
    }

}
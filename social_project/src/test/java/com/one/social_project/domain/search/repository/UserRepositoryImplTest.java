package com.one.social_project.domain.search.repository;

import com.one.social_project.domain.search.UserSearchCondition;
import com.one.social_project.domain.search.dto.UserSearchDTO;
import com.one.social_project.domain.user.entity.User;
import com.one.social_project.domain.user.repository.UserRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@Transactional
class UserRepositoryImplTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @Autowired
    private UserRepository userRepository;


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
    @DisplayName("닉네임으로 유저 검색")
    public void selectByParam() {
        UserSearchCondition condition = new UserSearchCondition();
        condition.setNickname("u");

        // 페이지 번호와 크기를 지정하여 Pageable 생성
        Pageable pageable = PageRequest.of(0, 4);

        // searchUser 메서드 호출
        Page<UserSearchDTO> result = userRepository.searchUserByNickname(condition, pageable);

        Assertions.assertThat(result.getSize()).isEqualTo(4);

    }

}
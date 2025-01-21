package com.one.social_project.domain.search.repository;

import com.one.social_project.domain.search.UserSearchCondition;
import com.one.social_project.domain.search.dto.QUserSearchDTO;
import com.one.social_project.domain.search.dto.UserSearchDTO;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.one.social_project.domain.user.entity.QUser.*;

@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public UserRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<UserSearchDTO> searchUserByNickname(UserSearchCondition condition, Pageable pageable) {
        List<UserSearchDTO> content = queryFactory
                .select(new QUserSearchDTO(
                        user.id.as("userId"),
                        user.nickname,
                        user.email,
                        user.profileImg
                ))
                .from(user)
                .where(
                        nicknameLike(condition.getNickname()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        System.out.println("content = " + content);

        long total = queryFactory
                .select(user)
                .from(user)
                .where(nicknameLike(condition.getNickname()))
                .fetchCount();


        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression nicknameLike(String nickname) {
        if (nickname == null) {
            return null;
        }
        return user.nickname.like("%" + nickname + "%");
    }

}

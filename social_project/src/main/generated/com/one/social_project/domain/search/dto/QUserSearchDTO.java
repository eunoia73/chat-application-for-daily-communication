package com.one.social_project.domain.search.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.one.social_project.domain.search.dto.QUserSearchDTO is a Querydsl Projection type for UserSearchDTO
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QUserSearchDTO extends ConstructorExpression<UserSearchDTO> {

    private static final long serialVersionUID = -1971726033L;

    public QUserSearchDTO(com.querydsl.core.types.Expression<Long> userId, com.querydsl.core.types.Expression<String> nickname, com.querydsl.core.types.Expression<String> email, com.querydsl.core.types.Expression<String> profileImg) {
        super(UserSearchDTO.class, new Class<?>[]{long.class, String.class, String.class, String.class}, userId, nickname, email, profileImg);
    }

}


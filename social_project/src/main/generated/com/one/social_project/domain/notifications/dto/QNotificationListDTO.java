package com.one.social_project.domain.notifications.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.one.social_project.domain.notifications.dto.QNotificationListDTO is a Querydsl Projection type for NotificationListDTO
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QNotificationListDTO extends ConstructorExpression<NotificationListDTO> {

    private static final long serialVersionUID = 532819945L;

    public QNotificationListDTO(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<String> receiver, com.querydsl.core.types.Expression<String> sender, com.querydsl.core.types.Expression<String> message, com.querydsl.core.types.Expression<Boolean> isRead, com.querydsl.core.types.Expression<java.time.LocalDateTime> createdAt) {
        super(NotificationListDTO.class, new Class<?>[]{long.class, String.class, String.class, String.class, boolean.class, java.time.LocalDateTime.class}, id, receiver, sender, message, isRead, createdAt);
    }

}


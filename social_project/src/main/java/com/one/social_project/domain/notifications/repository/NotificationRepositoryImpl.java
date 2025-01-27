package com.one.social_project.domain.notifications.repository;

import com.one.social_project.domain.notifications.dto.NotificationListDTO;
import com.one.social_project.domain.notifications.dto.QNotificationListDTO;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.one.social_project.domain.notifications.entity.QNotification.*;

@Repository
public class NotificationRepositoryImpl implements NotificationRepositoryCustom{
    private final JPAQueryFactory queryFactory;
    public NotificationRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<NotificationListDTO> findByReceiver(String userNickname, Pageable pageable) {
        QueryResults<NotificationListDTO> results = queryFactory
                .select(new QNotificationListDTO(
                        notification.id,
                        notification.notificationId,
                        notification.receiver,
                        notification.sender,
                        notification.message,
                        notification.isRead,
                        notification.createdAt
                ))
                .from(notification)
                .where(notification.receiver.eq(userNickname))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<NotificationListDTO> content = results.getResults();
        long total = results.getTotal();
        return new PageImpl<>(content, pageable, total);

    }
}

package com.one.social_project.domain.notifications.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QNotification is a Querydsl query type for Notification
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotification extends EntityPathBase<Notification> {

    private static final long serialVersionUID = 1181975512L;

    public static final QNotification notification = new QNotification("notification");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isRead = createBoolean("isRead");

    public final StringPath message = createString("message");

    public final StringPath notificationId = createString("notificationId");

    public final StringPath receiver = createString("receiver");

    public final StringPath roomId = createString("roomId");

    public final StringPath roomName = createString("roomName");

    public final EnumPath<com.one.social_project.domain.chat.constant.ChatRoomType> roomType = createEnum("roomType", com.one.social_project.domain.chat.constant.ChatRoomType.class);

    public final StringPath sender = createString("sender");

    public QNotification(String variable) {
        super(Notification.class, forVariable(variable));
    }

    public QNotification(Path<? extends Notification> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNotification(PathMetadata metadata) {
        super(Notification.class, metadata);
    }

}


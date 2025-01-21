package com.one.social_project.domain.chat.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChatRoom is a Querydsl query type for ChatRoom
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatRoom extends EntityPathBase<ChatRoom> {

    private static final long serialVersionUID = 614460896L;

    public static final QChatRoom chatRoom = new QChatRoom("chatRoom");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<ChatParticipants, QChatParticipants> participants = this.<ChatParticipants, QChatParticipants>createList("participants", ChatParticipants.class, QChatParticipants.class, PathInits.DIRECT2);

    public final StringPath roomId = createString("roomId");

    public final StringPath roomName = createString("roomName");

    public final EnumPath<com.one.social_project.domain.chat.constant.ChatRoomType> roomType = createEnum("roomType", com.one.social_project.domain.chat.constant.ChatRoomType.class);

    public QChatRoom(String variable) {
        super(ChatRoom.class, forVariable(variable));
    }

    public QChatRoom(Path<? extends ChatRoom> path) {
        super(path.getType(), path.getMetadata());
    }

    public QChatRoom(PathMetadata metadata) {
        super(ChatRoom.class, metadata);
    }

}


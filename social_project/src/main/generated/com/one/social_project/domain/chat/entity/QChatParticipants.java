package com.one.social_project.domain.chat.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChatParticipants is a Querydsl query type for ChatParticipants
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatParticipants extends EntityPathBase<ChatParticipants> {

    private static final long serialVersionUID = -1392275547L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChatParticipants chatParticipants = new QChatParticipants("chatParticipants");

    public final EnumPath<com.one.social_project.domain.chat.constant.ChatRole> chatRole = createEnum("chatRole", com.one.social_project.domain.chat.constant.ChatRole.class);

    public final QChatRoom chatRoom;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath userId = createString("userId");

    public QChatParticipants(String variable) {
        this(ChatParticipants.class, forVariable(variable), INITS);
    }

    public QChatParticipants(Path<? extends ChatParticipants> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChatParticipants(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChatParticipants(PathMetadata metadata, PathInits inits) {
        this(ChatParticipants.class, metadata, inits);
    }

    public QChatParticipants(Class<? extends ChatParticipants> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.chatRoom = inits.isInitialized("chatRoom") ? new QChatRoom(forProperty("chatRoom")) : null;
    }

}


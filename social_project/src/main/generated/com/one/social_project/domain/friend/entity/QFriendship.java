package com.one.social_project.domain.friend.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFriendship is a Querydsl query type for Friendship
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFriendship extends EntityPathBase<Friendship> {

    private static final long serialVersionUID = 506512365L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFriendship friendship = new QFriendship("friendship");

    public final NumberPath<Long> counterpartId = createNumber("counterpartId", Long.class);

    public final StringPath friendEmail = createString("friendEmail");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isFrom = createBoolean("isFrom");

    public final EnumPath<com.one.social_project.domain.friend.status.FriendshipStatus> status = createEnum("status", com.one.social_project.domain.friend.status.FriendshipStatus.class);

    public final com.one.social_project.domain.user.entity.QUser user;

    public final StringPath userEmail = createString("userEmail");

    public QFriendship(String variable) {
        this(Friendship.class, forVariable(variable), INITS);
    }

    public QFriendship(Path<? extends Friendship> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFriendship(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFriendship(PathMetadata metadata, PathInits inits) {
        this(Friendship.class, metadata, inits);
    }

    public QFriendship(Class<? extends Friendship> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.one.social_project.domain.user.entity.QUser(forProperty("user")) : null;
    }

}


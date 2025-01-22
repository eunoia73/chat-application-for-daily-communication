package com.one.social_project.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = 149147595L;

    public static final QUser user = new QUser("user");

    public final BooleanPath activated = createBoolean("activated");

    public final StringPath email = createString("email");

    public final ListPath<com.one.social_project.domain.friend.entity.Friendship, com.one.social_project.domain.friend.entity.QFriendship> friendshipList = this.<com.one.social_project.domain.friend.entity.Friendship, com.one.social_project.domain.friend.entity.QFriendship>createList("friendshipList", com.one.social_project.domain.friend.entity.Friendship.class, com.one.social_project.domain.friend.entity.QFriendship.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isFirstLogin = createBoolean("isFirstLogin");

    public final StringPath nickname = createString("nickname");

    public final StringPath oauthId = createString("oauthId");

    public final StringPath oauthProvider = createString("oauthProvider");

    public final StringPath oauthToken = createString("oauthToken");

    public final StringPath password = createString("password");

    public final StringPath profileImg = createString("profileImg");

    public final StringPath role = createString("role");

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}


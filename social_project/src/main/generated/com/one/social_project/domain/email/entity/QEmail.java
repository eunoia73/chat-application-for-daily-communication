package com.one.social_project.domain.email.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QEmail is a Querydsl query type for Email
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEmail extends EntityPathBase<Email> {

    private static final long serialVersionUID = -1992313061L;

    public static final QEmail email1 = new QEmail("email1");

    public final StringPath email = createString("email");

    public final BooleanPath emailStatus = createBoolean("emailStatus");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QEmail(String variable) {
        super(Email.class, forVariable(variable));
    }

    public QEmail(Path<? extends Email> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEmail(PathMetadata metadata) {
        super(Email.class, metadata);
    }

}


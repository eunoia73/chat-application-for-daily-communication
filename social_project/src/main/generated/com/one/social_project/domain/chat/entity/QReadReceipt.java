package com.one.social_project.domain.chat.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QReadReceipt is a Querydsl query type for ReadReceipt
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReadReceipt extends EntityPathBase<ReadReceipt> {

    private static final long serialVersionUID = 1541309205L;

    public static final QReadReceipt readReceipt = new QReadReceipt("readReceipt");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath messageId = createString("messageId");

    public final DateTimePath<java.time.LocalDateTime> readAt = createDateTime("readAt", java.time.LocalDateTime.class);

    public final StringPath userId = createString("userId");

    public QReadReceipt(String variable) {
        super(ReadReceipt.class, forVariable(variable));
    }

    public QReadReceipt(Path<? extends ReadReceipt> path) {
        super(path.getType(), path.getMetadata());
    }

    public QReadReceipt(PathMetadata metadata) {
        super(ReadReceipt.class, metadata);
    }

}


package com.one.social_project.domain.file.entity;

import com.one.social_project.domain.chat.entity.ChatMessage;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "files")
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileCategory category;  // ENUM 타입으로 파일 카테고리 설정

    @Column(nullable = false)
    private String fileName;

    private String fileType;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private Long fileSize;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id")
//    private User user;  // 파일을 업로드한 사용자와의 관계 (ManyToOne)
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "chat_id")
//    private ChatMessage chatMessage;  // 채팅 메시지와의 관계 (OneToOne)

    @Column(nullable = true)  // chatMessageId를 null 가능하게 설정
    private Long chatMessageId;

    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;


    //==연관관계 메서드==//

}


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

    @Column(nullable = false)
    private String fileId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileCategory category;  // ENUM 타입으로 파일 카테고리 설정

    @Column(nullable = false)
    private String fileName;

    private String fileType;

    @Column(nullable = false)
    private String originFileUrl;  //원본 url

    @Column(nullable = true)
    private String thumbNailUrl;  //썸네일 url

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String nickname;  //파일을 업로드한 사용자 닉네임

    @Column(nullable = true)  // roomId를 null 가능하게 설정
    private String roomId;  //채팅에서 보냈을 경우, roomId

    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;



}


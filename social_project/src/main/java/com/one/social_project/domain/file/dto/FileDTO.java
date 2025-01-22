package com.one.social_project.domain.file.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.one.social_project.domain.file.entity.FileCategory;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.InputStream;
import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FileDTO {

    private Long id;
    private String nickname;
    private String fileName;
    private String fileType;
    private Long fileSize;
    @JsonIgnore
    private InputStream fileInputStream;
    private String fileUrl;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private FileCategory category;

//    private Long userId;

}

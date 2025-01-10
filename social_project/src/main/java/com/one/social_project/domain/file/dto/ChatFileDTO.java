package com.one.social_project.domain.file.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.io.InputStream;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class ChatFileDTO {

    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    @JsonIgnore
    private InputStream fileInputStream;
    private String fileUrl;
    @CreatedDate
    private LocalDateTime createdAt;

    //    private String category;
//    private Long userId;
//    private Long chatMessageId;

}

package com.one.social_project.domain.file.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ChatFileDTO extends FileDTO {
    private String roomId;

}

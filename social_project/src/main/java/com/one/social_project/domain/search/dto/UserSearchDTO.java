package com.one.social_project.domain.search.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class UserSearchDTO {
    private Long userId;
    private String nickname;
    private String email;
    private String profileImg;

    @QueryProjection
    public UserSearchDTO(Long userId, String nickname, String email, String profileImg) {
        this.userId = userId;
        this.nickname = nickname;
        this.email = email;
        this.profileImg = profileImg;
    }
}

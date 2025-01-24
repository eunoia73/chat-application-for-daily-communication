package com.one.social_project.domain.user.dto.response;

import java.util.List;

import com.one.social_project.domain.friend.entity.Friendship;
import com.one.social_project.domain.user.entity.User;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDtoResponse {
    private Long id;
    private String nickname;
    private String email;
    private Boolean isFirstLogin;
    private String role;
    private String profileImg;
    private boolean activated;
    private String oauthProvider;
    private List<Friendship> friendshipList;

    public static UserDtoResponse from(User user) {
        return UserDtoResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .isFirstLogin(user.getIsFirstLogin())
                .role(user.getRole())
                .profileImg(user.getProfileImg())
                .activated(user.isActivated())
                .oauthProvider(user.getOauthProvider())
                .friendshipList(user.getFriendshipList())
                .build();
    }
} 
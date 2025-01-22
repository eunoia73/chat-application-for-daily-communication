package com.one.social_project.domain.user.dto.user;

import com.one.social_project.domain.user.entity.User;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@Builder
public class UserDto {

    private Long id;
    private String nickname;
    private String email;
    private String password;
    private Boolean isFirstLogin;
    private String role;
    private String profileImg;
    private boolean activated;

    // OAuth 관련 필드 추가
    private String oauthProvider;  // 예: "google", "facebook"
    private String oauthId;        // OAuth 제공자에서 반환하는 고유 식별자
    private String oauthToken;     // OAuth 액세스 토큰 (선택적, 저장하는 것이 보통 아님)

    public User toEntity()
    {
        return User.builder()
                .id(id)
                .nickname(nickname)
                .email(email)
                .password(password)
                .isFirstLogin(isFirstLogin)
                .role(role)
                .profileImg(profileImg)
                .activated(activated)
                .oauthProvider(oauthProvider)
                .oauthId(oauthId)
                .oauthToken(oauthToken)
                .build();
    }
}

package com.one.social_project.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_entity")
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    // 일반 회원가입과 OAuth 가입을 구분할 수 있도록 변경된 생성자
    public User(String email, String password, String nickname, boolean isFirstLogin, String role, boolean activated) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.isFirstLogin = isFirstLogin;
        this.role = role;
        this.activated = activated;
    }

    // OAuth 사용자를 위한 생성자 추가
    public User(String email, String nickname, String oauthProvider, String role, String oauthId, boolean activated) {
        this.email = email;
        this.nickname = nickname;
        this.oauthProvider = oauthProvider;
        this.role = role;
        this.oauthId = oauthId;
        this.activated = activated;
    }

    public void changePassword(String password) {
        this.password = password;
    }
}

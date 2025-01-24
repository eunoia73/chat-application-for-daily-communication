package com.one.social_project.domain.user.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.one.social_project.domain.friend.entity.Friendship;
import com.one.social_project.domain.user.dto.response.UserDtoResponse;
import com.one.social_project.domain.user.dto.user.UserDto;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_entity", indexes = @Index(name = "idx_user_nickname", columnList = "nickname"))
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

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonManagedReference  // 무한 참조 방지
    private List<Friendship> friendshipList;


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

    public UserDto toDto() {
        return UserDto.builder()
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

    public UserDtoResponse toResponseDto() {
        return UserDtoResponse.from(this);
    }
}

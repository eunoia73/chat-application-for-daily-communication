package com.one.social_project.domain.user.dto;

public class OAuth2UserInfo {

    private String email;
    private String nickname;
    private String oauthId; // OAuth2 제공자에서 반환하는 고유 식별자
    private String provider; // 예: "google", "facebook"

    // 생성자
    public OAuth2UserInfo(String email, String nickname, String oauthId) {
        this.email = email;
        this.nickname = nickname;
        this.oauthId = oauthId;
    }

    // getter & setter
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getOauthId() {
        return oauthId;
    }

    public void setOauthId(String oauthId) {
        this.oauthId = oauthId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}

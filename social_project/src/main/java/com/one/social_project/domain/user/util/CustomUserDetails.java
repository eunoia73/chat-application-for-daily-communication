package com.one.social_project.domain.user.util;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import com.one.social_project.domain.user.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class CustomUserDetails implements UserDetails, OAuth2User {

    private final User user;
    private final Map<String, Object> attributes;

    public CustomUserDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.getRole()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;  // 사용자 활성화 여부를 체크할 수 있음
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;  // OAuth2User에서 제공하는 attributes 반환
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    // 추가로 필요한 사용자 정보 메서드
    public String getNickname() {
        return user.getNickname();
    }

    public String getProfileImg() {
        return user.getProfileImg();
    }
}

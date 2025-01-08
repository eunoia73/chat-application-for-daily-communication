package com.one.social_project.domain.user.dto;

import com.one.social_project.domain.user.entity.UserEntity;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String email;
    private String password;
    private String role;

    // UserEntity -> UserDto 변환
    public static UserDto toDto(UserEntity user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .role(user.getRole())
                .build();
    }

    // UserDto -> UserEntity 변환
    public UserEntity toEntity() {
        return UserEntity.builder()
                .id(id)
                .email(email)
                .password(password)
                .role(role)
                .build();
    }
}

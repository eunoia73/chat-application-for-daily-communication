package com.one.social_project.domain.user.entity;

import com.one.social_project.domain.user.dto.CustomUserDetails;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@Table(name = "user")
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Email
    @Column(length = 20, nullable = false)
    private String email;

    @Column( nullable = false)
    private String password;

    private String role;

    public UserEntity(CustomUserDetails userDTO) {
        this.email = userDTO.getUsername();
        this.password = userDTO.getPassword();
        this.role = userDTO.getAuthorities().iterator().next().getAuthority();
    }


}
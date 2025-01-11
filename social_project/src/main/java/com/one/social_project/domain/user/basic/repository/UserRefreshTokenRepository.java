package com.one.social_project.domain.user.basic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.one.social_project.domain.user.basic.entity.UserRefreshToken;
import com.one.social_project.domain.user.basic.entity.Users;

import java.util.Optional;

public interface UserRefreshTokenRepository extends JpaRepository<UserRefreshToken, Long> {
    Optional<UserRefreshToken> findByUserAndReIssueCountLessThan(Users user, int count);
    Optional<UserRefreshToken> findByUser(Users user);
    Optional<UserRefreshToken> findByAccessToken(String accessToken);
}

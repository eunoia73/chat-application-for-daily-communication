package com.one.social_project.domain.user.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.one.social_project.domain.user.user.entity.UserRefreshToken;
import com.one.social_project.domain.user.user.entity.Users;

import java.util.Optional;

public interface UserRefreshTokenRepository extends JpaRepository<UserRefreshToken, Long> {
    Optional<UserRefreshToken> findByUserAndReIssueCountLessThan(Users user, int count);
    Optional<UserRefreshToken> findByUser(Users user);
}

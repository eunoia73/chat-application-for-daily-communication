package com.one.social_project.domain.user.repository;


import com.one.social_project.domain.search.repository.UserRepositoryCustom;
import com.one.social_project.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {

    Optional<User> findByEmail(String email);
    List<User> findUsersByEmail(String email);
    Optional<User> findByNickname(String nickname);
    Boolean existsByEmail(String email);
    Boolean existsByNickname(String nickname);

    Optional<User> findByOauthProviderAndOauthId(String oauthProvider, String oauthId);

}

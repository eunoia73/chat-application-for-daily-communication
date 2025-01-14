package com.one.social_project.domain.user.basic.repository;


import com.one.social_project.domain.user.basic.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByEmail(String email);
    List<Users> findUsersByEmail(String email);
    Optional<Users> findByNickname(String nickname);

}

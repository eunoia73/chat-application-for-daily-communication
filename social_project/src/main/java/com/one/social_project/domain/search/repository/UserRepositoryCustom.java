package com.one.social_project.domain.search.repository;

import com.one.social_project.domain.search.UserSearchCondition;
import com.one.social_project.domain.search.dto.UserSearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {
    Page<UserSearchDTO> searchUserByNickname(UserSearchCondition condition, Pageable pageable);
}

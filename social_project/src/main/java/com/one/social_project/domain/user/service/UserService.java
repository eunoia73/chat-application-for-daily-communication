package com.one.social_project.domain.user.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.one.social_project.domain.user.dto.user.UserDto;
import com.one.social_project.domain.user.entity.User;
import com.one.social_project.domain.user.repository.UserRepository;
import com.one.social_project.exception.errorCode.UserErrorCode;
import com.one.social_project.exception.exception.UserException;
import com.one.social_project.domain.user.dto.response.UserDtoResponse;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class UserService{

    private final UserRepository userRepository;


    public UserDto getUserInfo(User user) {
        return user.toDto();
    }

    public String changeProfileImage(Long user ,String profileImage) {
        User findUser = userRepository.findById(user)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        findUser.setProfileImg(profileImage);
        userRepository.save(findUser);
        return profileImage;
    }

    public List<UserDtoResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(User::toResponseDto)
                .toList();
    }

}

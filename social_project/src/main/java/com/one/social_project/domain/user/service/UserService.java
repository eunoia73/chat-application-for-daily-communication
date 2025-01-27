package com.one.social_project.domain.user.service;

import java.util.List;

import com.one.social_project.domain.user.dto.util.CheckDto;
import org.springframework.stereotype.Service;

import com.one.social_project.domain.user.dto.user.UserDto;
import com.one.social_project.domain.user.entity.User;
import com.one.social_project.domain.user.repository.UserRepository;
import com.one.social_project.domain.user.util.CustomUserDetails;
import com.one.social_project.domain.user.util.RedisSessionManager;
import com.one.social_project.domain.user.util.TokenProvider;
import com.one.social_project.exception.errorCode.UserErrorCode;
import com.one.social_project.exception.exception.UserException;
import com.one.social_project.domain.user.dto.response.UserDtoResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Service
@RequiredArgsConstructor
public class UserService{

    private final UserRepository userRepository;
    private final String s3Url = "https://sookyung-s3-bucket.s3.ap-northeast-2.amazonaws.com/";

    public UserDto getUserInfo(User user) {
        return user.toDto();
    }

    public String changeProfileImage(Long user ,String profileImage) {
        User findUser = userRepository.findById(user)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        findUser.setProfileImg(s3Url + profileImage);
        userRepository.save(findUser);
        return profileImage;
    }

    public List<UserDtoResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(User::toResponseDto)
                .toList();
    }

    public CheckDto isValidEmail(String email) {
        CheckDto checkDto = new CheckDto();
        checkDto.setResult(userRepository.findByEmail(email).isPresent());
        return checkDto;
    }

    public CheckDto isValidNickname(String nickname) {
        CheckDto checkDto = new CheckDto();
        checkDto.setResult(userRepository.findByNickname(nickname).isPresent());
        return checkDto;

    }

}

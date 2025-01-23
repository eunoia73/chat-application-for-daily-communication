package com.one.social_project.domain.user.service;

import com.one.social_project.domain.user.dto.OAuth2UserInfo;
import com.one.social_project.domain.user.dto.login.LoginReqDto;
import com.one.social_project.domain.user.dto.login.LoginResDto;
import com.one.social_project.domain.user.dto.register.RegisterReqDto;
import com.one.social_project.domain.user.dto.register.RegisterResDto;
import com.one.social_project.domain.user.dto.user.UserDto;
import com.one.social_project.domain.user.entity.User;
import com.one.social_project.domain.user.repository.UserRepository;
import com.one.social_project.domain.user.util.CustomUserDetails;
import com.one.social_project.domain.user.util.RedisSessionManager;
import com.one.social_project.domain.user.util.TokenProvider;
import com.one.social_project.exception.errorCode.UserErrorCode;
import com.one.social_project.exception.exception.UserException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
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

    public boolean isValidEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean isValidNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

}

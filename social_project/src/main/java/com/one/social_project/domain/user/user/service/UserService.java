package com.one.social_project.domain.user.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.one.social_project.domain.user.util.CustomUserDetails;
import com.one.social_project.domain.user.user.dto.*;
import com.one.social_project.domain.user.user.entity.UserRefreshToken;
import com.one.social_project.domain.user.user.entity.Users;
import com.one.social_project.domain.user.user.repository.UserRefreshTokenRepository;
import com.one.social_project.domain.user.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.one.social_project.domain.user.filter.JWTTokenValidatorFilter;
import com.one.social_project.domain.user.util.RedisSessionManager;
import java.nio.file.AccessDeniedException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final Environment env;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final JWTTokenValidatorFilter jwtTokenValidatorFilter;
    private final RedisSessionManager redisSessionManager;
    private RedisTemplate<String, String> redisTemplate;

    public UserDto mypage(CustomUserDetails user) throws AccessDeniedException {
        if(user == null) {
            throw new AccessDeniedException("액세스 거부");
        }
        Users tempUser = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException(user.getEmail()));
        UserDto userDto = new UserDto();
        userDto.setId(tempUser.getId());
        userDto.setEmail(tempUser.getEmail());
        userDto.setUsername(user.getUsername());
        userDto.setPassword(passwordEncoder.encode(user.getPassword()));
        userDto.setActivated(tempUser.isActivated());
        userDto.setRole(tempUser.getRole());
        return userDto;
    }

    @Transactional
    public SignInResponse signIn(SignInRequest signInRequest) {


        String accessJwtToken;
        String refreshJwtToken = "";

        // 사용자가 입력한 아이디와 비밀번호를 통해서 인증작업 진행
        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.unauthenticated(signInRequest.getEmail(), signInRequest.getPassword());

        // 사용자가 입력한 데이터로 수행한 인증 결과를 반환한다.
        Authentication authenticationResponse = authenticationManager.authenticate(authentication);

        // 인증이 성공적으로 수행되었다면 다음 블록을 수행
        if(null != authenticationResponse && authenticationResponse.isAuthenticated()) {

            Users user = userRepository.findByEmail(authenticationResponse.getName())
                    .orElseThrow(() -> new IllegalStateException("입력한 이메일에 해당하는 사용자가 없습니다."));

            user.setIsFirstLogin(false);
            // JWT Access Token 생성
            accessJwtToken = tokenProvider.createAccessToken(
                    user.getId(),
                    authenticationResponse.getName(),
                    authenticationResponse.getAuthorities().stream().map(
                            GrantedAuthority::getAuthority).collect(Collectors.joining(",")));

            // JWT Refresh Token 생성
            String refreshToken = tokenProvider.createRefreshToken();
            refreshJwtToken = refreshToken;

            // 이미 DB에 저장중인 Refresh Token 이 있다면 갱신하고, 없다면 DB에 추가하기
            userRefreshTokenRepository.findByUser(user).ifPresentOrElse(
                    it -> it.updateRefreshToken(refreshToken),
                    () -> userRefreshTokenRepository.save(new UserRefreshToken(user, accessJwtToken, refreshToken))
            );
        } else {
            accessJwtToken = "";
        }
        return new SignInResponse(signInRequest.getEmail(), accessJwtToken, refreshJwtToken);	// 생성자에 토큰 추가
    }

    // 회원 가입 기능
    public UserDto register(UserRegisterDto userRegisterDto) {

        // 이미 사용 중인 이메일일 경우에는 예외 반환
        userRepository.findByEmail(userRegisterDto.getEmail()).ifPresent(user -> {
            throw new RuntimeException("User already exists.");
        });

        // 이미 사용 중인 닉네임일 경우에는 예외 반환
        userRepository.findByNickname(userRegisterDto.getNickname()).ifPresent(user -> {
            throw new RuntimeException("Nickname already exists.");
        });


        Users newUser = new Users(
                userRegisterDto.getEmail(),
                userRegisterDto.getPassword(),
                userRegisterDto.getNickname(),
                true,
                "user",
                true
        );

        Users user= userRepository.save(newUser);

        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setUsername(user.getNickname());
        userDto.setPassword(passwordEncoder.encode(user.getPassword()));
        userDto.setRole(user.getRole());
        userDto.setActivated(user.isActivated());

        Users register = userRepository.findByEmail(user.getEmail()).orElseThrow(()-> new RuntimeException(user.getEmail()));

        UserRefreshToken userRefreshToken = new UserRefreshToken(register,"","");
        userRefreshTokenRepository.save(userRefreshToken);


        return userDto;
    }

    // 비밀 번호 변경 메소드
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest passwordChangeRequest) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));
        // 비밀 번호 변경
        String newEncodedPassword = passwordEncoder.encode(passwordChangeRequest.getPassword());
        user.changePassword(newEncodedPassword);

    }

}

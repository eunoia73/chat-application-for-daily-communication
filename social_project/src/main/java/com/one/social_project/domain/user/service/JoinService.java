package com.one.social_project.domain.user.service;

import com.one.social_project.domain.user.dto.JoinDto;
import com.one.social_project.domain.user.entity.UserEntity;
import com.one.social_project.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class JoinService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public void joinProcess(JoinDto joinDto) throws Exception {
        String email = joinDto.getEmail();
        String password = joinDto.getPassword();

        if (!userRepository.findUserEntitiesByEmail(email).isEmpty()) {

            throw new RuntimeException("이미 존재하는 유저입니다.");
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(email);
        userEntity.setPassword(bCryptPasswordEncoder.encode(password));
        userEntity.setRole("USER");

        userRepository.save(userEntity);
    }
}
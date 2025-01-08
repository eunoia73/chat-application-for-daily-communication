package com.one.social_project.domain.user.service;

import com.one.social_project.domain.user.dto.CustomUserDetails;
import com.one.social_project.domain.user.entity.UserEntity;
import com.one.social_project.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity userData = userRepository.findUserEntitiesByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
        return new CustomUserDetails(userData);
    }
}

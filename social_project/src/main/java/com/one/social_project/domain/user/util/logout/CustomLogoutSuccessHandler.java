package com.one.social_project.domain.user.util.logout;

import com.one.social_project.domain.user.ApplicationConstants;
import com.one.social_project.domain.user.entity.User;
import com.one.social_project.domain.user.util.RedisSessionManager;
import com.one.social_project.domain.user.repository.UserRepository;
import com.one.social_project.domain.user.util.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import java.io.IOException;
import java.util.Optional;

import lombok.*;

@RequiredArgsConstructor
@Component
public class CustomLogoutSuccessHandler implements LogoutHandler, LogoutSuccessHandler {

    private final RedisSessionManager redisSessionManager;
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        String header = request.getHeader("Authorization");
        String accessToken =  parseBearerToken(header);
        System.out.println("헤더 : "+accessToken);

        String email = tokenProvider.getEmailFromAccessToken(accessToken);
        System.out.println("email : "+email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("찾을 수 없음"));
        redisSessionManager.addToBlacklist(accessToken);
    }


    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException {

        if (authentication != null) {
            String username = authentication.getName(); // 로그아웃하는 사용자의 이름
        }

        // 로그아웃 완료 메시지를 JSON 형식으로 응답 본문에 전달
        response.setStatus(HttpServletResponse.SC_OK);  // HTTP 상태 코드 200 OK
        response.setContentType("application/json");    // 응답 형식은 JSON
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\": \"로그아웃 완료\"}");  // 메시지 전송

        // 리다이렉션 없이, 프론트엔드에서 해당 응답을 처리하도록 합니다.
    }

    private String parseBearerToken(String headerName) {

        if (headerName != null && headerName.startsWith("Bearer")) {
            return headerName.substring(6);  // "Bearer " 부분을 잘라냄
        }
        return null;
    }

}
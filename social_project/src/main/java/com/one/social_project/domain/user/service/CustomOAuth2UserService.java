package com.one.social_project.domain.user.service;

import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.one.social_project.domain.user.dto.OAuth2UserInfo;
import com.one.social_project.domain.user.repository.UserRepository;
import com.one.social_project.domain.user.util.config.GoogleOAuthConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import com.one.social_project.domain.user.entity.User;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    private static final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";


    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//        OAuth2User oAuth2User = super.loadUser(userRequest);
//        String registrationId = userRequest.getClientRegistration().getRegistrationId();  // 예: "google"
//        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();  // "sub"
//
//        Map<String, Object> attributes = oAuth2User.getAttributes();
//
//        String email = (String) attributes.get("email");
//        String nickname = (String) attributes.get("name");
//        String oauthId = (String) attributes.get(userNameAttributeName);
//
//        // DB에서 사용자 정보 조회
//        Optional<User> userOptional = userRepository.findByOauthProviderAndOauthId(registrationId, oauthId);
//        User user = userOptional.orElseGet(() -> new User(email, nickname, registrationId, oauthId, true));
//
//        // 기존의 유저가 아닌 경우 새로 저장
//        if (user.getId() == null) {
//            user.setActivated(true);
//            userRepository.save(user);
//        }

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
//                attributes,
//                userNameAttributeName
                null,
                null
        );
    }

    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";

    public String getAccessToken(String authorizationCode) {

        GoogleOAuthConfig googleOAuthConfig = new GoogleOAuthConfig();
        // Google API에 access token 요청을 위한 body 설정
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", authorizationCode);  // authorization code
        body.add("client_id", clientId);  // Google OAuth client ID
        body.add("client_secret", clientSecret);  // Google OAuth client secret
        body.add("redirect_uri", redirectUri);  // redirect URI
        body.add("grant_type", "authorization_code");  // authorization code grant type

        // HTTP 요청을 보낼 RestTemplate 준비
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        // POST 요청을 보내서 Access Token 받아오기
        ResponseEntity<String> response = restTemplate.exchange(GOOGLE_TOKEN_URL, HttpMethod.POST, entity, String.class);

        // 응답에서 access token을 추출 (JSON 응답에서 access_token 필드 값)
        String accessToken = extractAccessToken(response.getBody());
        return accessToken;
    }

    private String extractAccessToken(String responseBody) {
        // Google API 응답에서 access_token을 추출하는 로직 (JSON 파싱)
        // 예시로 Gson을 사용
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
        return jsonObject.get("access_token").getAsString();
    }



    public OAuth2UserInfo getUserInfoFromGoogle(String accessToken) {
        // HTTP 요청을 보내서 사용자 정보 받기
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(GOOGLE_USER_INFO_URL, HttpMethod.GET, entity, String.class);

        // 응답에서 사용자 정보 추출 (JSON 파싱)
        return extractUserInfo(response.getBody());
    }

    private OAuth2UserInfo extractUserInfo(String responseBody) {
        // Google API 응답에서 사용자 정보를 추출하는 로직 (JSON 파싱)
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

        String email = jsonObject.get("email").getAsString();
        String nickname = jsonObject.get("name").getAsString();
        String oauthId = jsonObject.get("sub").getAsString();  // 구글의 고유 ID (oauthId)

        return new OAuth2UserInfo(email, nickname, oauthId);
    }
}
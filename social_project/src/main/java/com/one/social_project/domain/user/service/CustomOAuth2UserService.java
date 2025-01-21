package com.one.social_project.domain.user.service;

import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.one.social_project.domain.user.dto.OAuth2UserInfo;
import com.one.social_project.domain.user.util.properties.GoogleOAuth2Properties;
import com.one.social_project.domain.user.util.properties.KakaoOAuth2Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final GoogleOAuth2Properties googleOAuth2Properties;
    private final KakaoOAuth2Properties kakaoOAuthConfig;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 부모 클래스(DefaultOAuth2UserService)를 통해 기본 사용자 정보 로드
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 사용자 정보 가져오기
        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // 예: 'google', 'kakao'
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName(); // 식별자 키 (예: 'sub', 'id')

        // OAuth2User의 attributes 값을 로드
        var attributes = oAuth2User.getAttributes(); // Google 또는 Kakao에서 반환한 JSON 데이터

        // 적절한 권한 부여 및 사용자 정보 반환
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                userNameAttributeName // 식별자 키
        );
    }

    public OAuth2UserInfo getUserInfoFromOAuth(String provider, String authorizationCode) {
        String accessToken = getAccessTokenFromOAuth(provider, authorizationCode);
        return getUserInfoFromProvider(provider, accessToken);
    }

    private String getAccessTokenFromOAuth(String provider, String authorizationCode) {
        if ("kakao".equalsIgnoreCase(provider)) {
            return getAccessTokenFromKakao(authorizationCode);
        } else if ("google".equalsIgnoreCase(provider)) {
            return getAccessTokenFromGoogle(authorizationCode);
        }
        throw new IllegalArgumentException("Unsupported provider: " + provider);
    }

    private String getAccessTokenFromKakao(String authorizationCode) {
        return getAccessToken(authorizationCode, kakaoOAuthConfig.getTokenUrl(), kakaoOAuthConfig.getClientId(), kakaoOAuthConfig.getClientSecret(), kakaoOAuthConfig.getRedirectUri());
    }

    private String getAccessTokenFromGoogle(String authorizationCode) {
        return getAccessToken(authorizationCode, googleOAuth2Properties.getTokenUrl(), googleOAuth2Properties.getClientId(), googleOAuth2Properties.getClientSecret(), googleOAuth2Properties.getRedirectUri());
    }

    private String getAccessToken(String authorizationCode, String tokenUrl, String clientId, String clientSecret, String redirectUri) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("grant_type", "authorization_code");
        body.add("code", authorizationCode);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, String.class);
        return extractAccessToken(response.getBody());
    }

    private String extractAccessToken(String responseBody) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
        return jsonObject.get("access_token").getAsString();
    }

    private OAuth2UserInfo getUserInfoFromProvider(String provider, String accessToken) {
        if ("kakao".equalsIgnoreCase(provider)) {
            return getUserInfoFromKakao(accessToken);
        } else if ("google".equalsIgnoreCase(provider)) {
            return getUserInfoFromGoogle(accessToken);
        }
        throw new IllegalArgumentException("Unsupported provider: " + provider);
    }

    private OAuth2UserInfo getUserInfoFromGoogle(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(googleOAuth2Properties.getUserInfoUrl(), HttpMethod.GET, entity, String.class);
        return extractUserInfo(response.getBody(), "google");
    }

    private OAuth2UserInfo getUserInfoFromKakao(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(kakaoOAuthConfig.getUserInfoUrl(), HttpMethod.GET, entity, String.class);
        return extractUserInfo(response.getBody(), "kakao");
    }

    private OAuth2UserInfo extractUserInfo(String responseBody, String provider) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

        String email = jsonObject.get("email").getAsString();
        String nickname = jsonObject.get("name").getAsString();
        String oauthId;

        if ("google".equals(provider)) {
            oauthId = jsonObject.get("sub").getAsString(); // 구글의 고유 ID
        } else if ("kakao".equals(provider)) {
            oauthId = jsonObject.get("id").getAsString(); // 카카오의 고유 ID
        } else {
            throw new IllegalArgumentException("Unsupported provider for user info extraction");
        }

        return new OAuth2UserInfo(email, nickname, oauthId);
    }
}


package ru.maruchekas.keycloak.service;

import lombok.RequiredArgsConstructor;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.maruchekas.keycloak.api.request.AuthRequest;
import ru.maruchekas.keycloak.api.request.RefreshTokenRequest;
import ru.maruchekas.keycloak.exception.InvalidTokenException;

@Service
@RequiredArgsConstructor
public class KeycloakClientService {
    @Value("${keycloak.auth-server-url}")
    private String keyCloakUrl;
    @Value("${keycloak.resource}")
    private String clientId;
    @Value("${keycloak.realm}")
    private String realm;

    private final RestTemplate restTemplate;

    public AccessTokenResponse authenticate(AuthRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("username",request.getUsername());
        parameters.add("password",request.getPassword());
        parameters.add("grant_type", "password");
        parameters.add("client_id", request.getClientId());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(parameters, headers);

        // should catch exception
        return restTemplate.exchange(getAuthUrl(),
                HttpMethod.POST,
                entity,
                AccessTokenResponse.class).getBody();
    }

    public AccessTokenResponse refreshToken(RefreshTokenRequest request) throws InvalidTokenException {
        AccessTokenResponse accessTokenResponse;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("grant_type", "refresh_token");
        parameters.add("client_id", clientId);
        parameters.add("refresh_token", request.getRefreshToken());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(parameters, headers);


        try {
            accessTokenResponse = restTemplate.exchange(getAuthUrl(),
                    HttpMethod.POST,
                    entity,
                    AccessTokenResponse.class).getBody();
        } catch (Exception e){
            throw new InvalidTokenException("invalid or expired refresh token");
        }

        return accessTokenResponse;
    }

    public AccessTokenResponse logout(RefreshTokenRequest request) throws InvalidTokenException {
        AccessTokenResponse accessTokenResponse;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("client_id", clientId);
        parameters.add("refresh_token", request.getRefreshToken());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(parameters, headers);

        try {
            accessTokenResponse = restTemplate.exchange(getLogoutUrl(),
                    HttpMethod.POST,
                    entity,
                    AccessTokenResponse.class).getBody();
        } catch (Exception e){
            throw new InvalidTokenException("invalid or expired refresh token");
        }

        return accessTokenResponse;
    }

    private String getAuthUrl() {
        return UriComponentsBuilder.fromHttpUrl(keyCloakUrl)
                .pathSegment("realms")
                .pathSegment(realm)
                .pathSegment("protocol")
                .pathSegment("openid-connect")
                .pathSegment("token")
                .toUriString();
    }

    private String getLogoutUrl() {
        return UriComponentsBuilder.fromHttpUrl(keyCloakUrl)
                .pathSegment("realms")
                .pathSegment(realm)
                .pathSegment("protocol")
                .pathSegment("openid-connect")
                .pathSegment("logout")
                .toUriString();
    }
}

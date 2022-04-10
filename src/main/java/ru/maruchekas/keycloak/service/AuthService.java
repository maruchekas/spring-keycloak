package ru.maruchekas.keycloak.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import ru.maruchekas.keycloak.exception.AuthenticationDataException;
import ru.maruchekas.keycloak.exception.InvalidTokenException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    @Value("${keycloak.auth-server-url}")
    private String keyCloakUrl;
    @Value("${keycloak.resource}")
    private String clientId;
    @Value("${keycloak.realm}")
    private String realm;

    private final RestTemplate restTemplate;

    public AccessTokenResponse authenticate(AuthRequest authRequest) throws AuthenticationDataException {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", authRequest.getUsername());
        map.add("password", authRequest.getPassword());
        map.add("client_id", authRequest.getClientId());
        map.add("grant_type","password");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        return restTemplate.postForObject(getFullUrl("token"), request, AccessTokenResponse.class);

    }

    public AccessTokenResponse refreshToken(RefreshTokenRequest request) throws InvalidTokenException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("grant_type", "refresh_token");
        parameters.add("client_id", clientId);
        parameters.add("refresh_token", request.getRefreshToken());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(parameters, headers);

        return restTemplate.exchange(getFullUrl("token"),
                HttpMethod.POST,
                entity,
                AccessTokenResponse.class).getBody();
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
            accessTokenResponse = restTemplate.exchange(getFullUrl("logout"),
                    HttpMethod.POST,
                    entity,
                    AccessTokenResponse.class).getBody();
        } catch (Exception e) {
            throw new InvalidTokenException();
        }

        return accessTokenResponse;
    }

    private String getUserInfo(String accessToken) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", accessToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, headers);
        return restTemplate.postForObject(getFullUrl("userinfo"), request, String.class);
    }

    public List<String> getRoles(String accessToken) throws Exception {
        String response = getUserInfo(accessToken);

        Map map = new ObjectMapper().readValue(response, HashMap.class);
        return (List<String>) map.get("roles");
    }

    private String getFullUrl(String tail) {
        return UriComponentsBuilder.fromHttpUrl(keyCloakUrl)
                .pathSegment("realms")
                .pathSegment(realm)
                .pathSegment("protocol")
                .pathSegment("openid-connect")
                .pathSegment(tail)
                .toUriString();
    }

}

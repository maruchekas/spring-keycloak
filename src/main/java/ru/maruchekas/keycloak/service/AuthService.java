package ru.maruchekas.keycloak.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
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
import ru.maruchekas.keycloak.dto.UserDTO;
import ru.maruchekas.keycloak.exception.AuthenticationDataException;
import ru.maruchekas.keycloak.exception.InvalidTokenException;

@Slf4j
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
        map.add("grant_type", "password");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        return restTemplate.postForObject(getFullUrl("token"), request, AccessTokenResponse.class);

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
            accessTokenResponse = restTemplate.exchange(getFullUrl("token"),
                    HttpMethod.POST,
                    entity,
                    AccessTokenResponse.class).getBody();
        } catch (RuntimeException e) {
            throw  new InvalidTokenException();
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
            accessTokenResponse = restTemplate.exchange(getFullUrl("logout"),
                    HttpMethod.POST,
                    entity,
                    AccessTokenResponse.class).getBody();
        } catch (Exception e) {
            throw new InvalidTokenException();
        }

        return accessTokenResponse;
    }

    public UserDTO getUserInfo(String accessToken) {
        String stringResponse;
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", accessToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, headers);
        stringResponse = restTemplate.postForObject(getFullUrl("userinfo"), request, String.class);
        if (stringResponse == null){
            throw new AuthenticationDataException();
        }
        JSONObject jsonUser = new JSONObject(stringResponse);
        return new UserDTO()
                .setUserId(jsonUser.getString("sub"))
                .setUserName(jsonUser.getString("preferred_username"));
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

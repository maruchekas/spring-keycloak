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
import ru.maruchekas.keycloak.dto.UserDTO;
import ru.maruchekas.keycloak.exception.AuthenticationDataException;

import java.net.http.HttpResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${keycloak.auth-server-url}")
    private String keyCloakUrl;
    @Value("${keycloak.resource}")
    private String clientId;
    @Value("${keycloak.realm}")
    private String realm;

    private final RestTemplate restTemplate;

    public AccessTokenResponse addUserToGroup(String groupId, String userId, String accessToken){
        HttpHeaders headers = getAuthHeaders(accessToken);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);
        String url = UriComponentsBuilder.fromHttpUrl(keyCloakUrl)
                .pathSegment("admin")
                .pathSegment("realms")
                .pathSegment(realm)
                .pathSegment("users")
                .pathSegment(userId)
                .pathSegment("groups")
                .pathSegment(groupId)
                .toUriString();

        return restTemplate.exchange(url, HttpMethod.PUT, entity, AccessTokenResponse.class).getBody();
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

    private HttpHeaders getAuthHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);
        return headers;
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

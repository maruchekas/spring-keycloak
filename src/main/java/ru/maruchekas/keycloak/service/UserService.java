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
import ru.maruchekas.keycloak.entity.User;
import ru.maruchekas.keycloak.exception.AuthenticationDataException;
import ru.maruchekas.keycloak.exception.FailedGetMembersException;

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

    public void addUserToGroup(String groupId, String userId, String accessToken){
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

        restTemplate.exchange(url, HttpMethod.PUT, entity, AccessTokenResponse.class).getBody();
    }

    public User getUserById(String userId, String accessToken) {
        HttpHeaders headers = getAuthHeaders(accessToken);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);
        String url = UriComponentsBuilder.fromHttpUrl(keyCloakUrl)
                .pathSegment("admin")
                .pathSegment("realms")
                .pathSegment(realm)
                .pathSegment("users")
                .pathSegment(userId)
                .toUriString();

        String stringResponse = restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
        if (stringResponse == null) {
            throw new FailedGetMembersException();
        }
        JSONObject jsonUser = new JSONObject(stringResponse);

        return new User()
                .setUserId(jsonUser.getString("id"))
                .setUserName(jsonUser.getString("username"));
    }

    public UserDTO userToUserDTO(String userId, String accessToken) {
        User user = getUserById(userId, accessToken);
        return new UserDTO()
                .setUserId(user.getUserId())
                .setUserName(user.getUserName());
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

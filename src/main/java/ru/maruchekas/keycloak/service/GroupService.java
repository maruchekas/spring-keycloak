package ru.maruchekas.keycloak.service;

import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.maruchekas.keycloak.api.request.CreateGroupRequest;
import ru.maruchekas.keycloak.entity.Group;

@Service
@RequiredArgsConstructor
public class GroupService {

    @Value("${keycloak.auth-server-url}")
    private String keyCloakUrl;
    @Value("${keycloak.resource}")
    private String clientId;
    @Value("${keycloak.realm}")
    private String realm;

    private final RestTemplate restTemplate;

    public ResponseEntity<String> getAllGroups(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", accessToken);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);

        return restTemplate.exchange(getBaseGroupUrl(),
                HttpMethod.GET,
                entity,
                String.class);
    }

    public Group getGroupById(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> stringResp = restTemplate.exchange(getBaseGroupUrl(),
                HttpMethod.GET,
                entity,
                String.class);

        JSONArray jsonArray = new JSONArray(stringResp.getBody());
        JSONObject jsonObject = jsonArray.getJSONObject(0);


        return new Group()
                .setId(jsonObject.getString("id"))
                .setName(jsonObject.getString("name"))
                .setPath(jsonObject.getString("path"));
    }

    public AccessTokenResponse createGroup(CreateGroupRequest createGroupRequest, String accessToken){

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("name", createGroupRequest.getName());
        body.add("path", createGroupRequest.getPath());
        body.add("subGroups", null);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(getBaseGroupUrl(),
                HttpMethod.POST,
                entity,
                AccessTokenResponse.class).getBody();
    }

    public AccessTokenResponse deleteGroupById(String accessToken, String id){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", accessToken);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);

        return restTemplate.exchange(getBaseGroupUrlWithId(id),
                HttpMethod.DELETE,
                entity,
                AccessTokenResponse.class).getBody();
    }

    private String getBaseGroupUrlWithId(String id) {
        return UriComponentsBuilder.fromHttpUrl(keyCloakUrl)
                .pathSegment("admin")
                .pathSegment("realms")
                .pathSegment(realm)
                .pathSegment("groups")
                .pathSegment(id)
                .toUriString();
    }

    private String getBaseGroupUrl() {
        return UriComponentsBuilder.fromHttpUrl(keyCloakUrl)
                .pathSegment("admin")
                .pathSegment("realms")
                .pathSegment(realm)
                .pathSegment("groups")
                .toUriString();
    }

}

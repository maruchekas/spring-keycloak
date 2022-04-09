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
import ru.maruchekas.keycloak.dto.GroupDTO;
import ru.maruchekas.keycloak.dto.UserDTO;
import ru.maruchekas.keycloak.entity.Group;

import java.util.ArrayList;
import java.util.List;

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

    public List<GroupDTO> getAllGroups(String accessToken) {
        HttpHeaders headers = getAuthHeaders(accessToken, MediaType.APPLICATION_JSON);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);

        String stringResponse =
                restTemplate.exchange(getBaseGroupUrl(), HttpMethod.GET, entity, String.class).getBody();

        JSONArray groupArrayJson = new JSONArray(stringResponse);

        return mapResponseToListGroups(groupArrayJson);
    }

    public GroupDTO getGroupById(String accessToken, String id) {
        HttpHeaders headers = getAuthHeaders(accessToken, MediaType.APPLICATION_JSON);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> stringResp = restTemplate.exchange(getBaseGroupUrlWithId(id),
                HttpMethod.GET,
                entity,
                String.class);

        JSONObject jsonObject = new JSONObject(stringResp.getBody());
        Group group = mapResponseToGroup(jsonObject);

        return mapGroupToDTO(group).setUsers(getGroupMembersByGroupId(accessToken,id));
    }

    public List<UserDTO> getGroupMembersByGroupId(String accessToken, String id) {
        HttpHeaders headers = getAuthHeaders(accessToken, MediaType.APPLICATION_JSON);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);

        JSONArray membersResponse = new JSONArray(restTemplate.exchange(getBaseGroupUrlWithId(id) + "/members",
                HttpMethod.GET,
                entity,
                String.class).getBody());

        return mapMembersToUserDTOList(membersResponse);
    }

    public AccessTokenResponse createGroup(CreateGroupRequest createGroupRequest, String accessToken){

        HttpHeaders headers = getAuthHeaders(accessToken, MediaType.APPLICATION_JSON);

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

    public AccessTokenResponse updateGroupById(String accessToken, String id, CreateGroupRequest request) {
        HttpHeaders headers = getAuthHeaders(accessToken, MediaType.APPLICATION_JSON);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("name", request.getName());
        body.add("path", request.getPath());
        body.add("subGroups", null);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(getBaseGroupUrlWithId(id),
                HttpMethod.PUT,
                entity,
                AccessTokenResponse.class).getBody();
    }

    public AccessTokenResponse deleteGroupById(String accessToken, String id){
        HttpHeaders headers = getAuthHeaders(accessToken, MediaType.APPLICATION_JSON);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);

        return restTemplate.exchange(getBaseGroupUrlWithId(id),
                HttpMethod.DELETE,
                entity,
                AccessTokenResponse.class).getBody();
    }

    private HttpHeaders getAuthHeaders(String accessToken, MediaType type){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(type);
        headers.set("Authorization", accessToken);
        return headers;
    }

    private Group mapResponseToGroup(JSONObject groupJson){
        return new Group()
                .setId(groupJson.getString("id"))
                .setName(groupJson.getString("name"))
                .setPath(groupJson.getString("path"));
    }

    private GroupDTO mapGroupToDTO(Group group){
        return new GroupDTO()
                .setId(group.getId())
                .setName(group.getName())
                .setPath(group.getPath())
                .setAccess(group.getAccess());
    }

    private List<GroupDTO> mapResponseToListGroups(JSONArray groupsJson){
        List<GroupDTO> groupDTOList = new ArrayList<>();
        for (Object o : groupsJson) {
            Group group = mapResponseToGroup((JSONObject) o);
            groupDTOList.add(mapGroupToDTO(group));
        }

        return groupDTOList;
    }

    private List<UserDTO> mapMembersToUserDTOList(JSONArray membersResponse){
        List<UserDTO> userDTOList = new ArrayList<>();
        for (Object o : membersResponse) {
            userDTOList.add(mapJsonToUserDTO((JSONObject) o));
        }

        return userDTOList;
    }

    private UserDTO mapJsonToUserDTO(JSONObject rawUser){
        String firstName = rawUser.has("firstName") ? rawUser.getString("firstName") : null;
        String lastName = rawUser.has("lastName") ? rawUser.getString("lastName") : null;
        String email = rawUser.has("email") ? rawUser.getString("email") : null;

        return new UserDTO()
                .setId(rawUser.getString("id"))
                .setUsername(rawUser.getString("username"))
                .setFirstName(firstName)
                .setLastName(lastName)
                .setEmail(email);
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

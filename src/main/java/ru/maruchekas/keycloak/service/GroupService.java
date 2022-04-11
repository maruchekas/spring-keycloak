package ru.maruchekas.keycloak.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.maruchekas.keycloak.api.request.ChangeGroupStatusListRequest;
import ru.maruchekas.keycloak.api.request.CreateGroupRequest;
import ru.maruchekas.keycloak.api.response.BlockStatusGroupResponse;
import ru.maruchekas.keycloak.dto.GroupDTO;
import ru.maruchekas.keycloak.dto.UserDTO;
import ru.maruchekas.keycloak.entity.Access;
import ru.maruchekas.keycloak.entity.Group;
import ru.maruchekas.keycloak.exception.FailedCreateGroupFromJsonException;
import ru.maruchekas.keycloak.exception.FailedGetGroupFromJsonException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GroupService {

    @Value("${keycloak.auth-server-url}")
    private String keyCloakUrl;
    @Value("${keycloak.realm}")
    private String realm;

    private final RestTemplate restTemplate;

    public List<GroupDTO> getAllGroups(String accessToken) {
        HttpHeaders headers = getAuthHeaders(accessToken, MediaType.APPLICATION_JSON);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);

        String stringResponse =
                restTemplate.exchange(getBaseGroupUrl(), HttpMethod.GET, entity, String.class).getBody();
        JSONArray groupArrayJson = new JSONArray(stringResponse);

        return mapResponseToListGroups(groupArrayJson, accessToken);
    }

    public GroupDTO getGroupById(String accessToken, String id) {
        HttpHeaders headers = getAuthHeaders(accessToken, MediaType.APPLICATION_JSON);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> stringResp = restTemplate.exchange(getBaseGroupUrlWithId(id),
                HttpMethod.GET,
                entity,
                String.class);

        JSONObject groupAsJson = new JSONObject(stringResp.getBody());
        Group group = mapResponseToGroup(groupAsJson);

        return mapGroupToDTO(group)
                .setUsers(getGroupMembersByGroupId(accessToken, id))
                .setAccess(mapJsonToAccess(groupAsJson));
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

    public AccessTokenResponse createGroup(CreateGroupRequest createGroupRequest, String accessToken) {

        HttpHeaders headers = getAuthHeaders(accessToken, MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("name", createGroupRequest.getName());

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(getBaseGroupUrl(),
                HttpMethod.POST,
                entity,
                AccessTokenResponse.class).getBody();
    }

    public BlockStatusGroupResponse changeBlockStatusGroup(String accessToken, ChangeGroupStatusListRequest changeGroupStatusRequest) {

        List<Group> groups = new ArrayList<>();
        /**
         * TODO получить список групп из респонса, поменять статусы(проверить возможность), заполнить ответ
         */

        return new BlockStatusGroupResponse();
    }

    public AccessTokenResponse updateGroupById(String accessToken, String id, CreateGroupRequest request) {
        HttpHeaders headers = getAuthHeaders(accessToken, MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("name", request.getName());

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(getBaseGroupUrlWithId(id),
                HttpMethod.PUT,
                entity,
                AccessTokenResponse.class).getBody();
    }

    public AccessTokenResponse deleteGroupById(String accessToken, String id) {
        HttpHeaders headers = getAuthHeaders(accessToken, MediaType.APPLICATION_JSON);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);

        return restTemplate.exchange(getBaseGroupUrlWithId(id),
                HttpMethod.DELETE,
                entity,
                AccessTokenResponse.class).getBody();
    }

    private HttpHeaders getAuthHeaders(String accessToken, MediaType type) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(type);
        headers.set("Authorization", accessToken);
        return headers;
    }

    private Group mapResponseToGroup(JSONObject groupJson) {
        if (groupJson.isEmpty()
                || !groupJson.has("id") || !groupJson.has("name") || !groupJson.has("path")){
            throw new FailedGetGroupFromJsonException();
        }
        if (StringUtils.isBlank(groupJson.getString("id")) || StringUtils.isBlank(groupJson.getString("name"))
                || StringUtils.isBlank(groupJson.getString("path"))) {
            throw new FailedCreateGroupFromJsonException();
        }

        return new Group()
                .setId(groupJson.getString("id"))
                .setName(groupJson.getString("name"))
                .setPath(groupJson.getString("path"))
                .setCreatedBy(LocalDateTime.now());
    }

    private GroupDTO mapGroupToDTO(Group group) {
        return new GroupDTO()
                .setId(group.getId())
                .setName(group.getName())
                .setPath(group.getPath())
                .setSubGroups(new ArrayList<>())
                .setGroupAdmin(new ArrayList<>())
                .setGroupAuditor(new ArrayList<>())
                .setPolicies(new ArrayList<>())
                .setCreateDateTime(group.getCreatedBy())
                .setAccess(group.getAccess());
    }

    private List<GroupDTO> mapResponseToListGroups(JSONArray groupsJson, String accessToken) {
        List<GroupDTO> groupDTOList = new ArrayList<>();
        for (Object o : groupsJson) {
            Group group = mapResponseToGroup((JSONObject) o);
            groupDTOList.add(mapGroupToDTO(group).setUsers(getGroupMembersByGroupId(accessToken, group.getId())));
        }

        return groupDTOList;
    }

    private List<UserDTO> mapMembersToUserDTOList(JSONArray membersJson) {
        List<UserDTO> userDTOList = new ArrayList<>();
        for (Object o : membersJson) {
            userDTOList.add(mapJsonToUserDTO((JSONObject) o));
        }

        return userDTOList;
    }

    private UserDTO mapJsonToUserDTO(JSONObject rawUser) {
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

    private Access mapJsonToAccess(JSONObject jsonObject) {
        JSONObject accessJson = (JSONObject) jsonObject.get("access");
        return new Access().setView(accessJson.getBoolean("view"))
                .setManage(accessJson.getBoolean("manage"))
                .setManageMembership(accessJson.getBoolean("manageMembership"));
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

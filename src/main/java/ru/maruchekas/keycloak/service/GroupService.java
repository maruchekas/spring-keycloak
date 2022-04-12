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
import java.util.*;

@Service
@RequiredArgsConstructor
public class GroupService {

    @Value("${keycloak.auth-server-url}")
    private String keyCloakUrl;
    @Value("${keycloak.realm}")
    private String realm;
    private final String keyId = "id";
    private final String keyName = "name";
    private final String keyPath = "path";
    private final String keyUsername = "username";
    private final String keyAccess = "access";
    private final String keyEmail = "email";
    private final String keyFirstName = "firstName";
    private final String keyLastName = "lastName";
    private final String keyMembers = "members";
    private final String keyRoleMapping = "role-mappings";
    private final String keyManage = "manage";
    private final String keyView = "view";
    private final String keyManageMembership = "manageMembership";

    private final RestTemplate restTemplate;

    public List<GroupDTO> getAllGroups(String accessToken) {
        HttpHeaders headers = getAuthHeaders(accessToken);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);
        String url = createBaseUrl().toUriString();

        String stringResponse =
                restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
        JSONArray groupArrayJson = new JSONArray(stringResponse);

        return mapResponseToListGroups(groupArrayJson, accessToken);
    }

    public GroupDTO getGroupById(String accessToken, String id) {
        HttpHeaders headers = getAuthHeaders(accessToken);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);
        String url = createBaseUrl().pathSegment(id).toUriString();

        ResponseEntity<String> stringResp = restTemplate.exchange(url,
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
        HttpHeaders headers = getAuthHeaders(accessToken);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);
        String url = createBaseUrl().pathSegment(id).pathSegment(keyMembers).toUriString();

        JSONArray membersResponse = new JSONArray(restTemplate.exchange(url,
                HttpMethod.GET,
                entity,
                String.class).getBody());

        return mapMembersToUserDTOList(membersResponse);
    }

    public AccessTokenResponse createGroup(CreateGroupRequest createGroupRequest, String accessToken) {

        HttpHeaders headers = getAuthHeaders(accessToken);

        Map<String, String> body = new HashMap<>();
        body.put(keyName, createGroupRequest.getName());

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        String url = createBaseUrl().toUriString();

        return restTemplate.exchange(url,
                HttpMethod.POST,
                entity,
                AccessTokenResponse.class).getBody();
    }

    public BlockStatusGroupResponse changeBlockStatusGroup(String accessToken,
                                                           ChangeGroupStatusListRequest changeGroupStatusRequest) {
        List<Group> groups = new ArrayList<>();
        /**
         * TODO получить список групп из реквеста, поменять статусы(проверить возможность), заполнить ответ
         */

        return new BlockStatusGroupResponse();
    }

    public AccessTokenResponse updateGroupById(String accessToken, String id, CreateGroupRequest request) {
        HttpHeaders headers = getAuthHeaders(accessToken);

        Map<String, String> body = new HashMap<>();
        body.put(keyName, request.getName());

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        String url = createBaseUrl().pathSegment(id).toUriString();

        return restTemplate.exchange(url,
                HttpMethod.PUT,
                entity,
                AccessTokenResponse.class).getBody();
    }

    public AccessTokenResponse deleteGroupById(String accessToken, String id) {
        HttpHeaders headers = getAuthHeaders(accessToken);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);
        String url = createBaseUrl().pathSegment(id).toUriString();

        return restTemplate.exchange(url,
                HttpMethod.DELETE,
                entity,
                AccessTokenResponse.class).getBody();
    }

    public List<String> getRoles(String accessToken, String id) {
        HttpHeaders headers = getAuthHeaders(accessToken);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);
        String url = createBaseUrl().pathSegment(id).pathSegment(keyRoleMapping).toUriString();

        String stringResponse = restTemplate.exchange(url,
                HttpMethod.GET,
                entity,
                String.class).getBody();

        JSONObject jsonResponse = new JSONObject(stringResponse);

        return mapJsonToRoles(jsonResponse);
    }

    private List<String> mapJsonToRoles(JSONObject jsonResponse) {
        List<String> roles = new ArrayList<>();
        if (jsonResponse.has("realmMappings")) {
            JSONArray jsonAsRoles = jsonResponse.getJSONArray("realmMappings");

            for (Object o : jsonAsRoles) {
                JSONObject jsonAsRole = (JSONObject) o;
                String role = jsonAsRole.getString("name");
                roles.add(role);
            }
        }
        return roles;
    }

    private HttpHeaders getAuthHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);
        return headers;
    }

    private Group mapResponseToGroup(JSONObject groupJson) {
        if (groupJson.isEmpty()
                || !groupJson.has(keyId) || !groupJson.has(keyName) || !groupJson.has(keyPath)) {
            throw new FailedGetGroupFromJsonException();
        }
        if (StringUtils.isBlank(groupJson.getString(keyId)) || StringUtils.isBlank(groupJson.getString(keyName))
                || StringUtils.isBlank(groupJson.getString(keyPath))) {
            throw new FailedCreateGroupFromJsonException();
        }

        return new Group()
                .setId(groupJson.getString(keyId))
                .setName(groupJson.getString(keyName))
                .setPath(groupJson.getString(keyPath))
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
        String firstName = rawUser.has(keyFirstName) ? rawUser.getString(keyFirstName) : null;
        String lastName = rawUser.has(keyLastName) ? rawUser.getString(keyLastName) : null;
        String email = rawUser.has(keyEmail) ? rawUser.getString(keyEmail) : null;

        return new UserDTO()
                .setId(rawUser.getString(keyId))
                .setUsername(rawUser.getString(keyUsername))
                .setFirstName(firstName)
                .setLastName(lastName)
                .setEmail(email);
    }

    private Access mapJsonToAccess(JSONObject jsonObject) {
        JSONObject accessJson = (JSONObject) jsonObject.get(keyAccess);
        return new Access().setView(accessJson.getBoolean(keyView))
                .setManage(accessJson.getBoolean(keyManage))
                .setManageMembership(accessJson.getBoolean(keyManageMembership));
    }

    private UriComponentsBuilder createBaseUrl() {
        return UriComponentsBuilder.fromHttpUrl(keyCloakUrl)
                .pathSegment("admin")
                .pathSegment("realms")
                .pathSegment(realm)
                .pathSegment("groups");
    }

}

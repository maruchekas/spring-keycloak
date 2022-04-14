package ru.maruchekas.keycloak.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.maruchekas.keycloak.api.request.ChangeGroupStatusListRequest;
import ru.maruchekas.keycloak.api.request.CreateGroupData;
import ru.maruchekas.keycloak.api.request.CreateGroupDataRequest;
import ru.maruchekas.keycloak.api.request.DeleteGroupRequest;
import ru.maruchekas.keycloak.api.response.BlockStatusGroupResponse;
import ru.maruchekas.keycloak.api.response.GroupResponse;
import ru.maruchekas.keycloak.dto.GroupDTO;
import ru.maruchekas.keycloak.dto.UserDTO;
import ru.maruchekas.keycloak.entity.Attribute;
import ru.maruchekas.keycloak.entity.Group;
import ru.maruchekas.keycloak.entity.User;
import ru.maruchekas.keycloak.exception.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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
    private final String keyAttributes = "attributes";
    private final String keyEmail = "email";
    private final String keyFirstName = "firstName";
    private final String keyLastName = "lastName";
    private final String keyMembers = "members";
    private final String keyRoleMapping = "role-mappings";

    private final RestTemplate restTemplate;
    private final AuthService authService;
    private final UserService userService;

    public List<GroupDTO> getAllGroups(String accessToken) {
        HttpHeaders headers = getAuthHeaders(accessToken);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);
        String url = createBaseUrl().toUriString();

        String stringResponse =
                restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
        if (stringResponse == null) {
            throw new FailedGetListOfGroupsException();
        }
        JSONArray groupArrayJson = new JSONArray(stringResponse);

        return mapResponseToListGroups(groupArrayJson, accessToken);
    }

    public GroupDTO getGroupById(String accessToken, String id) {
        HttpHeaders headers = getAuthHeaders(accessToken);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);
        String url = createBaseUrl().pathSegment(id).toUriString();

        ResponseEntity<String> stringResponse = restTemplate.exchange(url,
                HttpMethod.GET,
                entity,
                String.class);

        if (stringResponse.getBody() == null) {
            throw new FailedGetListOfGroupsException();
        }
        JSONObject groupAsJson = new JSONObject(stringResponse.getBody());
        Group group = mapResponseToGroup(groupAsJson);

        return mapGroupToDTO(group)
                .setUsers(getGroupMembersByGroupId(accessToken, id));
    }

    public List<UserDTO> getGroupMembersByGroupId(String accessToken, String id) {
        HttpHeaders headers = getAuthHeaders(accessToken);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);
        String url = createBaseUrl().pathSegment(id).pathSegment(keyMembers).toUriString();

        String stringResponse = restTemplate.exchange(url,
                HttpMethod.GET,
                entity,
                String.class).getBody();
        if (stringResponse == null) {
            throw new FailedGetMembersException();
        }
        JSONArray membersResponse = new JSONArray(stringResponse);

        return mapMembersToUserDTOList(membersResponse);
    }

    public GroupResponse createGroup(CreateGroupDataRequest createGroupRequest, String accessToken) {

        HttpHeaders headers = getAuthHeaders(accessToken);
        GroupResponse response = new GroupResponse();
        Group group = new Group();

        for (CreateGroupData request : createGroupRequest.getGroups()) {
            group.setName(request.getGroupName());

            HttpEntity<Group> entity = new HttpEntity<>(group, headers);
            String url = createBaseUrl().toUriString();

            try {
                restTemplate.exchange(url,
                        HttpMethod.POST,
                        entity,
                        AccessTokenResponse.class).getStatusCode();
            } catch (HttpClientErrorException.Conflict exception) {
                throw new GroupAlreadyExistsException();
            }

            for (User user : request.getUsers()) {
                userService.addUserToGroup(getGroupIgByName(accessToken, request.getGroupName()), user.getUserId(),
                        accessToken);
            }
            response = new GroupResponse()
                    .setCode(HttpStatus.CREATED.value())
                    .setGroupName(getGroupIgByName(accessToken, request.getGroupName()))
                    .setUsers(request.getUsers())
                    .setCreatedBy(userService.getUserInfo(accessToken).getUserName())
                    .setCreatedAt(LocalDateTime.now());
        }

        return response;
    }


    public BlockStatusGroupResponse changeBlockStatusGroup(String accessToken,
                                                           ChangeGroupStatusListRequest changeGroupStatusRequest) {
        List<Group> groups = new ArrayList<>();
        /**
         * TODO получить список групп из реквеста, поменять статусы(проверить возможность), заполнить ответ
         */

        return new BlockStatusGroupResponse();
    }

//    public AccessTokenResponse updateGroupById(String accessToken, CreateGroupData createGroupData) {
//        HttpHeaders headers = getAuthHeaders(accessToken);
//        AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
//
//        for (GroupDTO groupDto : createGroupData) {
//            String updatedAtAsStr = "\"" + LocalDateTime.now() + "\"";
//
//            Attribute attribute = new Attribute()
//                    .setPolicies(groupDto.getPolicies())
//                    .setGroupAdmin(groupDto.getGroupAdmin())
//                    .setGroupAuditor(groupDto.getGroupAuditor())
//                    .setCreatedAt(List.of(groupDto.getCreatedAt()))
//                    .setCreatedBy(List.of(authService.getUserInfo(accessToken).getUserId()))
//                    .setUpdatedAt(List.of(updatedAtAsStr)).setUpdatedBy(List.of(""));
//
//            Group group = new Group()
//                    .setName(groupDto.getGroupName()).setAttributes(attribute);
//
//            HttpEntity<Group> entity = new HttpEntity<>(group, headers);
//            String url = createBaseUrl().pathSegment(groupDto.getGroupId()).toUriString();
//
//            try {
//                accessTokenResponse = restTemplate.exchange(url,
//                        HttpMethod.PUT,
//                        entity,
//                        AccessTokenResponse.class).getBody();
//            } catch (HttpClientErrorException.Conflict exception) {
//                throw new GroupAlreadyExistsException();
//            }
//        }
//        return accessTokenResponse;
//    }

    public AccessTokenResponse deleteGroupById(String accessToken, DeleteGroupRequest deleteRequest) {

        AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
        HttpHeaders headers = getAuthHeaders(accessToken);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);
        for (String groupId : deleteRequest.getGroupIds()) {
            String url = createBaseUrl().pathSegment(groupId).toUriString();

            accessTokenResponse = restTemplate.exchange(url,
                    HttpMethod.DELETE,
                    entity,
                    AccessTokenResponse.class).getBody();
        }
        return accessTokenResponse;
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

    private String getGroupIgByName(String accessToken, String name) {
        HttpHeaders headers = getAuthHeaders(accessToken);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);
        String url = createBaseUrl().toUriString();
        String groupId = "";

        String stringResponse =
                restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
        if (stringResponse == null) {
            throw new FailedGetListOfGroupsException();
        }
        JSONArray groupArrayJson = new JSONArray(stringResponse);

        for (Object o : groupArrayJson) {
            JSONObject rawGroup = (JSONObject) o;
            if (rawGroup.getString("name").equals(name)) {
                groupId = rawGroup.getString("id");
            }
        }

        return groupId;
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
                .setAttributes(mapJsonToGroupAttributes(groupJson));
    }

    private GroupDTO mapGroupToDTO(Group group) {
        GroupDTO groupDTO = new GroupDTO();
        Attribute attribute = new Attribute();
        if (group.getAttributes() != null) {
            attribute = group.getAttributes();
            groupDTO.setGroupAdmin(attribute.getGroupAdmin())
                    .setGroupAuditor(attribute.getGroupAuditor())
                    .setCreatedAt(attribute.getCreatedAt().get(0))
                    .setCreatedBy(attribute.getCreatedBy().get(0))
                    .setUpdatedAt(attribute.getUpdatedAt().get(0))
                    .setUpdatedBy(attribute.getUpdatedBy().get(0));
        }

        return new GroupDTO()
                .setGroupId(group.getId())
                .setGroupName(group.getName())
                .setPriority(group.getAttributes().getPriority())
                .setPolicies(group.getAttributes().getPolicies())
                .setGroupAdmin(group.getAttributes().getGroupAdmin())
                .setGroupAuditor(group.getAttributes().getGroupAuditor())
                .setCreatedBy(group.getAttributes().getCreatedBy().get(0))
                .setCreatedAt(group.getAttributes().getCreatedAt().get(0))
                .setUpdatedAt(group.getAttributes().getUpdatedAt().get(0))
                .setUpdatedBy(group.getAttributes().getUpdatedBy().get(0))
                .setBlocked(group.getAttributes().isBlocked())
                .setSoftDeleted(group.getAttributes().isSoftDeleted());
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
        String email = rawUser.has(keyEmail) ? rawUser.getString(keyEmail) : null;

        return new UserDTO()
                .setUserId(rawUser.getString(keyId))
                .setUserName(rawUser.getString(keyUsername))
                .setUserEmail(email);
    }

    private Attribute mapJsonToGroupAttributes(JSONObject jsonObject) {
        Attribute groupAttribute = new Attribute();
        JSONObject attributesJson;
        List<String> policies = new ArrayList<>();
        List<String> groupAdmin = new ArrayList<>();
        List<String> groupAuditor = new ArrayList<>();
        List<String> createdAt = new ArrayList<>();
        List<String> updatedAt = new ArrayList<>();
        List<String> createdBy = new ArrayList<>();
        List<String> updatedBy = new ArrayList<>();

        if (jsonObject.has(keyAttributes)) {
            attributesJson = (JSONObject) jsonObject.get(keyAttributes);

            if (attributesJson.has("policies")) {
                JSONArray arrayPolicies = attributesJson.getJSONArray("policies");
                for (Object o : arrayPolicies) {
                    policies.add((String) o);
                }
                groupAttribute.setPolicies(policies);
            }
            if (attributesJson.has("groupAdmin")) {
                JSONArray arrayGroupAdmin = attributesJson.getJSONArray("groupAdmin");
                for (Object o : arrayGroupAdmin) {
                    groupAdmin.add((String) o);
                }
                groupAttribute.setGroupAdmin(groupAdmin);
            }
            if (attributesJson.has("groupAuditor")) {
                JSONArray arrayGroupAuditor = attributesJson.getJSONArray("groupAuditor");
                for (Object o : arrayGroupAuditor) {
                    groupAuditor.add((String) o);
                }
                groupAttribute.setGroupAuditor(groupAuditor);
            }
            if (attributesJson.has("createdBy")) {
                JSONArray arrayCreatedBy = attributesJson.getJSONArray("createdBy");
                createdBy.add(arrayCreatedBy.getString(0));
                groupAttribute.setCreatedBy(createdBy);
            }
            if (attributesJson.has("updatedBy")) {
                JSONArray arrayUpdatedBy = attributesJson.getJSONArray("updatedBy");
                updatedBy.add(arrayUpdatedBy.getString(0));
                groupAttribute.setUpdatedBy(updatedBy);
            }
            if (attributesJson.has("createdAt")) {
                JSONArray arrayCreatedAt = attributesJson.getJSONArray("createdAt");
                for (Object o : arrayCreatedAt) {
                    String date = (String) o;
                    createdAt.add((date.replaceAll("\"", "")));
                }
                groupAttribute.setCreatedAt(createdAt);
            }
            if (attributesJson.has("updatedAt")) {
                JSONArray arrayUpdatedAt = attributesJson.getJSONArray("updatedAt");
                for (Object o : arrayUpdatedAt) {
                    String date = (String) o;
                    updatedAt.add((date.replaceAll("\"", "")));
                }
                groupAttribute.setUpdatedAt(updatedAt);
            }
            if (attributesJson.has("blocked")) {
                JSONArray arrayBlocked = attributesJson.getJSONArray("blocked");
                groupAttribute.setBlocked(arrayBlocked.getBoolean(0));
            }
            if (attributesJson.has("softDeleted")) {
                JSONArray arraySoftDeleted = attributesJson.getJSONArray("softDeleted");
                groupAttribute.setSoftDeleted(arraySoftDeleted.getBoolean(0));
            }
            if (attributesJson.has("priority")) {
                JSONArray arrayPriority = attributesJson.getJSONArray("priority");
                groupAttribute.setPriority(arrayPriority.getInt(0));
            }
        }

        return groupAttribute;
    }

    private UriComponentsBuilder createBaseUrl() {
        return UriComponentsBuilder.fromHttpUrl(keyCloakUrl)
                .pathSegment("admin")
                .pathSegment("realms")
                .pathSegment(realm)
                .pathSegment("groups");
    }

}

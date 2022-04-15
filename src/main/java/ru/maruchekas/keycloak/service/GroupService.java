package ru.maruchekas.keycloak.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.maruchekas.keycloak.api.request.CreateGroupRequest;
import ru.maruchekas.keycloak.api.request.CreateGroupDataRequest;
import ru.maruchekas.keycloak.api.request.DeleteGroupRequest;
import ru.maruchekas.keycloak.api.response.BlockStatusGroupResponse;
import ru.maruchekas.keycloak.api.response.GroupListResponse;
import ru.maruchekas.keycloak.api.response.GroupResponse;
import ru.maruchekas.keycloak.dto.AttributeDTO;
import ru.maruchekas.keycloak.dto.GroupDTO;
import ru.maruchekas.keycloak.dto.PolicyDTO;
import ru.maruchekas.keycloak.dto.UserDTO;
import ru.maruchekas.keycloak.entity.*;
import ru.maruchekas.keycloak.exception.FailedGetGroupFromJsonException;
import ru.maruchekas.keycloak.exception.FailedGetListOfGroupsException;
import ru.maruchekas.keycloak.exception.FailedGetMembersException;
import ru.maruchekas.keycloak.exception.GroupAlreadyExistsException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private final String keyUsername = "username";
    private final String keyAttributes = "attributes";
    private final String keyEmail = "email";
    private final String keyFirstName = "firstName";
    private final String keyLastName = "lastName";
    private final String keyMembers = "members";
    private final String keyRoleMapping = "role-mappings";

    private final RestTemplate restTemplate;
    private final UserService userService;

    public GroupListResponse getAllGroups(String accessToken) {
        HttpHeaders headers = getAuthHeaders(accessToken);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);
        String url = createBaseUrl().toUriString();

        String stringResponse =
                restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
        if (stringResponse == null) {
            throw new FailedGetListOfGroupsException();
        }
        JSONArray groupArrayJson = new JSONArray(stringResponse);

        List<String> groups = new ArrayList<>();

        for (Object o : groupArrayJson) {
            JSONObject jsonObject = (JSONObject) o;
            groups.add(jsonObject.getString("id"));
        }

        List<GroupResponse> groupResponseList = new ArrayList<>();

        for (String groupId : groups) {
            groupResponseList.add(getGroupById(accessToken, groupId).setCode(null));
        }

        return new GroupListResponse().setGroups(groupResponseList).setCode(HttpStatus.OK.value());
    }

    public GroupResponse getGroupById(String accessToken, String id) {
        HttpHeaders headers = getAuthHeaders(accessToken);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);
        String url = createBaseUrl().pathSegment(id).toUriString();

        String stringResponse = restTemplate.exchange(url,
                HttpMethod.GET,
                entity,
                String.class).getBody();

        if (stringResponse == null) {
            throw new FailedGetListOfGroupsException();
        }
        JSONObject groupAsJson = new JSONObject(stringResponse);

        return mapGroupFromKeycloakToGroupResponse(groupAsJson, accessToken)
                .setCode(HttpStatus.OK.value());
    }

    private GroupResponse mapGroupFromKeycloakToGroupResponse(JSONObject groupJson, String accessToken) {
        if (groupJson.isEmpty()) {
            throw new FailedGetGroupFromJsonException();
        }

        String groupId = groupJson.getString(keyId);
        String groupName = groupJson.getString(keyName);
        List<User> users = getGroupMembersByGroupId(accessToken, groupId);
        List<String> rolesFromKeycloak = getRolesForGroup(accessToken, groupId);

        List<PolicyDTO> policies = new ArrayList<>();

        for (String p : rolesFromKeycloak) {
            policies.add(getRoleById(p, accessToken));
        }

        Attribute attribute = mapJsonToGroupAttributes(groupJson);

        List<String> groupAdminFromKeycloak = attribute.getGroupAdmin();
        List<GroupAdmin> groupAdmins = new ArrayList<>();
        for (String s : groupAdminFromKeycloak) {
            UserDTO userDTO = userService.userToUserDTO(s.split(" : ")[0], accessToken);
            groupAdmins.add(new GroupAdmin()
                    .setGroupAdminId(userDTO.getUserId())
                    .setGroupAdminName(userDTO.getUserName()));
        }

        List<String> groupAuditorsFromKeycloak = attribute.getGroupAuditor();
        List<GroupAuditor> groupAuditors = new ArrayList<>();
        for (String s : groupAuditorsFromKeycloak) {
            UserDTO userDTO = userService.userToUserDTO(s.split(" : ")[0], accessToken);
            groupAuditors.add(new GroupAuditor()
                    .setGroupAuditorId(userDTO.getUserId())
                    .setGroupAuditorName(userDTO.getUserName()));
        }


            return new GroupResponse()
                .setGroupId(groupId)
                .setGroupName(groupName)
                .setCreatedAt(attribute.getCreatedAt())
                .setCreatedBy(attribute.getCreatedBy().getUserName())
                .setPolicies(policies)
                .setBlocked(attribute.isBlocked())
                .setSoftDeleted(attribute.isSoftDeleted())
                .setPriority(attribute.getPriority())
                .setUsers(users)
                .setGroupAdmin(groupAdmins)
                .setGroupAuditor(groupAuditors);
    }

    public List<User> getGroupMembersByGroupId(String accessToken, String groupId) {
        HttpHeaders headers = getAuthHeaders(accessToken);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);
        String url = createBaseUrl().pathSegment(groupId).pathSegment(keyMembers).toUriString();

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

    public GroupListResponse createGroup(CreateGroupDataRequest createGroupRequest, String accessToken) {

        HttpHeaders headers = getAuthHeaders(accessToken);
        GroupListResponse groupsResponse = new GroupListResponse();
        List<GroupResponse> groups = new ArrayList<>();

        for (CreateGroupRequest request : createGroupRequest.getGroups()) {
            GroupDTO group = mapRequestToGroupDTO(request, accessToken);

            HttpEntity<GroupDTO> entity = new HttpEntity<>(group, headers);
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

            for (PolicyDTO role : request.getPolicies()) {
                addRoleToGroup(getGroupIgByName(accessToken, request.getGroupName()), role.getPolicyId(), accessToken);
            }

            GroupResponse response = new GroupResponse()
                    .setGroupId(getGroupIgByName(accessToken, request.getGroupName()))
                    .setGroupName(request.getGroupName())
                    .setUsers(request.getUsers())
                    .setPolicies(request.getPolicies())
                    .setGroupAdmin(request.getGroupAdmin())
                    .setGroupAuditor(request.getGroupAuditor())
                    .setCreatedBy(userService.getUserInfo(accessToken).getUserName())
                    .setCreatedAt(LocalDateTime.now());
            groups.add(response);
        }

        return groupsResponse.setCode(HttpStatus.CREATED.value()).setGroups(groups);
    }

    private GroupDTO mapRequestToGroupDTO(CreateGroupRequest request, String accessToken){
        GroupDTO group = new GroupDTO();
        AttributeDTO attribute = new AttributeDTO();
        long currentTime = ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault()).toInstant().toEpochMilli();


        List<String> priority = List.of(String.valueOf(request.getPriority()));
        List<String> createdBy = List.of(userService.getUserInfo(accessToken).getUserName());
        List<String> createdAt = List.of(String.valueOf(currentTime));
        List<String> blocked = List.of(String.valueOf(false));
        List<String> softDeleted = List.of(String.valueOf(false));
        List<String> groupAdmin = new ArrayList<>();
        List<String> groupAuditor = new ArrayList<>();
        List<String> policies = new ArrayList<>();

        for (GroupAdmin admin : request.getGroupAdmin()) {
            String strAdmin = admin.getGroupAdminId() + " : " + admin.getGroupAdminName();
            groupAdmin.add(strAdmin);
        }

        for (GroupAuditor auditor : request.getGroupAuditor()) {
            String strAuditor = auditor.getGroupAuditorId() + " : " + auditor.getGroupAuditorName();
            groupAuditor.add(strAuditor);
        }

        if (request.getPolicies() != null) {
            for (PolicyDTO p : request.getPolicies()) {
                String policy = p.getPolicyId() + " : " + p.getPolicyName();
                policies.add(policy);
            }
        }

        attribute.setPriority(priority)
                .setCreatedBy(createdBy)
                .setCreatedAt(createdAt)
                .setPolicies(policies)
                .setGroupAdmin(groupAdmin)
                .setGroupAuditor(groupAuditor)
                .setBlocked(blocked)
                .setSoftDeleted(softDeleted);

        group.setName(request.getGroupName())
                .setAttributes(attribute);

        return group;
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

    private PolicyDTO getRoleById(String roleId, String accessToken) {
        HttpHeaders headers = getAuthHeaders(accessToken);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);
        String url = UriComponentsBuilder.fromHttpUrl(keyCloakUrl)
                .pathSegment("admin")
                .pathSegment("realms")
                .pathSegment(realm)
                .pathSegment("roles-by-id")
                .pathSegment(roleId).toUriString();

        JSONObject roleJson = new JSONObject(Objects.requireNonNull(restTemplate.exchange(url,
                HttpMethod.GET,
                entity,
                String.class).getBody()));

        return new PolicyDTO()
                .setPolicyId(roleJson.getString("id"))
                .setPolicyName(roleJson.getString("name"));
    }

    private List<String> getRolesForGroup(String accessToken, String groupId) {
        HttpHeaders headers = getAuthHeaders(accessToken);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);
        String url = createBaseUrl().pathSegment(groupId).pathSegment(keyRoleMapping).toUriString();

        String stringResponse = restTemplate.exchange(url,
                HttpMethod.GET,
                entity,
                String.class).getBody();

        JSONObject jsonResponse = new JSONObject(stringResponse);

        return mapJsonToRoles(jsonResponse);
    }

    public String addRoleToGroup(String groupId, String roleId, String accessToken) {

        PolicyDTO policyDTO = getRoleById(roleId, accessToken);
        List<Policy> roles = List.of(new Policy()
                .setId(policyDTO.getPolicyId())
                .setName(policyDTO.getPolicyName())
                .setComposite(false)
                .setClientRole(false)
                .setContainerId(realm));

        HttpHeaders headers = getAuthHeaders(accessToken);
        HttpEntity<List<Policy>> entity = new HttpEntity<>(roles, headers);
        String url = UriComponentsBuilder.fromHttpUrl(keyCloakUrl)
                .pathSegment("admin")
                .pathSegment("realms")
                .pathSegment(realm)
                .pathSegment("groups")
                .pathSegment(groupId)
                .pathSegment("role-mappings")
                .pathSegment("realm").toUriString();


        return restTemplate.exchange(url,
                HttpMethod.POST,
                entity,
                String.class).getBody();
    }

    public String deleteRoleFromGroup(String groupId, String roleId, String accessToken) {

        PolicyDTO policyDTO = getRoleById(roleId, accessToken);
        List<Policy> roles = List.of(new Policy()
                .setId(policyDTO.getPolicyId())
                .setName(policyDTO.getPolicyName())
                .setComposite(false)
                .setClientRole(false)
                .setContainerId(realm));

        HttpHeaders headers = getAuthHeaders(accessToken);
        HttpEntity<List<Policy>> entity = new HttpEntity<>(roles, headers);
        String url = UriComponentsBuilder.fromHttpUrl(keyCloakUrl)
                .pathSegment("admin")
                .pathSegment("realms")
                .pathSegment(realm)
                .pathSegment("groups")
                .pathSegment(groupId)
                .pathSegment("role-mappings")
                .pathSegment("realm").toUriString();


        return restTemplate.exchange(url,
                HttpMethod.DELETE,
                entity,
                String.class).getBody();
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
                String role = jsonAsRole.getString("id");
                roles.add(role);
            }
        }
        return roles;
    }

//    private GroupDTO mapGroupToDTO(Group group) {
//        GroupDTO groupDTO = new GroupDTO();
//        Attribute attribute = new Attribute();
//        if (group.getAttributes() != null) {
//            attribute = group.getAttributes();
//            groupDTO.setGroupAdmin(attribute.getGroupAdmin())
//                    .setGroupAuditor(attribute.getGroupAuditor())
//                    .setCreatedAt(attribute.getCreatedAt().get(0))
//                    .setCreatedBy(attribute.getCreatedBy().get(0))
//                    .setUpdatedAt(attribute.getUpdatedAt().get(0))
//                    .setUpdatedBy(attribute.getUpdatedBy().get(0));
//        }
//
//        return new GroupDTO()
//                .setGroupId(group.getId())
//                .setGroupName(group.getName())
//                .setPriority(group.getAttributes().getPriority())
//                .setPolicies(group.getAttributes().getPolicies())
//                .setGroupAdmin(group.getAttributes().getGroupAdmin())
//                .setGroupAuditor(group.getAttributes().getGroupAuditor())
//                .setCreatedBy(group.getAttributes().getCreatedBy().get(0))
//                .setCreatedAt(group.getAttributes().getCreatedAt().get(0))
//                .setUpdatedAt(group.getAttributes().getUpdatedAt().get(0))
//                .setUpdatedBy(group.getAttributes().getUpdatedBy().get(0))
//                .setBlocked(group.getAttributes().isBlocked())
//                .setSoftDeleted(group.getAttributes().isSoftDeleted());
//    }

//    private List<GroupDTO> mapResponseToListGroups(JSONArray groupsJson, String accessToken) {
//        List<GroupDTO> groupDTOList = new ArrayList<>();
//
//        for (Object o : groupsJson) {
//            Group group = mapResponseToGroup((JSONObject) o);
//            groupDTOList.add(mapGroupToDTO(group).setUsers(getGroupMembersByGroupId(accessToken, group.getId())));
//        }
//
//        return groupDTOList;
//    }

    private List<User> mapMembersToUserDTOList(JSONArray membersJson) {
        List<User> userDTOList = new ArrayList<>();
        for (Object o : membersJson) {
            userDTOList.add(mapJsonToUserDTO((JSONObject) o));
        }

        return userDTOList;
    }

    private User mapJsonToUserDTO(JSONObject rawUser) {
        String email = rawUser.has(keyEmail) ? rawUser.getString(keyEmail) : null;

        return new User()
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

        if (jsonObject.has(keyAttributes)) {
            attributesJson = (JSONObject) jsonObject.get(keyAttributes);

                JSONArray arrayPolicies = attributesJson.getJSONArray("policies");
                for (Object o : arrayPolicies) {
                    policies.add((String) o);
                }
                groupAttribute.setPolicies(policies);

                JSONArray arrayGroupAdmin = attributesJson.getJSONArray("groupAdmin");
                for (Object o : arrayGroupAdmin) {
                    groupAdmin.add((String) o);
                }
                groupAttribute.setGroupAdmin(groupAdmin);

                JSONArray arrayGroupAuditor = attributesJson.getJSONArray("groupAuditor");
                for (Object o : arrayGroupAuditor) {
                    groupAuditor.add((String) o);
                }
                groupAttribute.setGroupAuditor(groupAuditor);

                JSONArray arrayCreatedBy = attributesJson.getJSONArray("createdBy");
                groupAttribute.setCreatedBy(new UserDTO().setUserName(arrayCreatedBy.getString(0)));

                JSONArray arrayCreatedAt = attributesJson.getJSONArray("createdAt");

                    String date = arrayCreatedAt.getString(0);
                    LocalDateTime createdAt =
                            Instant.ofEpochMilli(Long.parseLong(date)).atZone(ZoneId.systemDefault()).toLocalDateTime();

                groupAttribute.setCreatedAt(createdAt);

                JSONArray arrayBlocked = attributesJson.getJSONArray("blocked");
                groupAttribute.setBlocked(arrayBlocked.getBoolean(0));

                JSONArray arraySoftDeleted = attributesJson.getJSONArray("softDeleted");
                groupAttribute.setSoftDeleted(arraySoftDeleted.getBoolean(0));

                JSONArray arrayPriority = attributesJson.getJSONArray("priority");
                groupAttribute.setPriority(arrayPriority.getInt(0));

        }

        return groupAttribute;
    }

    private HttpHeaders getAuthHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);
        return headers;
    }

    private UriComponentsBuilder createBaseUrl() {
        return UriComponentsBuilder.fromHttpUrl(keyCloakUrl)
                .pathSegment("admin")
                .pathSegment("realms")
                .pathSegment(realm)
                .pathSegment("groups");
    }

}

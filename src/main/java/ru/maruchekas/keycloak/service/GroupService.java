package ru.maruchekas.keycloak.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.maruchekas.keycloak.api.request.*;
import ru.maruchekas.keycloak.api.response.BlockStatusGroupResponse;
import ru.maruchekas.keycloak.api.response.CommonResponse;
import ru.maruchekas.keycloak.api.response.GroupListResponse;
import ru.maruchekas.keycloak.api.response.GroupResponse;
import ru.maruchekas.keycloak.dto.*;
import ru.maruchekas.keycloak.entity.Attribute;
import ru.maruchekas.keycloak.entity.Group;
import ru.maruchekas.keycloak.exception.FailedGetListOfGroupsException;
import ru.maruchekas.keycloak.exception.FailedGetMembersException;
import ru.maruchekas.keycloak.exception.GroupAlreadyExistsException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private final String keyMembers = "members";

    private final RestTemplate restTemplate;
    private final UserService userService;

    public GroupResponse getGroupById(String groupId, String accessToken) {
        GroupDTO groupDTO = getGroupDTOById(groupId, accessToken);

        return groupDtoToResponse(groupDTO).setCode(0);
    }

    public GroupListResponse getGroupsByQuery(FilterRequest filter, String accessToken) {
        List<GroupDTO> groups = findAllGroups(accessToken);
        List<GroupResponse> groupResponseList = new ArrayList<>();

        for (GroupDTO group : groups) {
            if (!group.getAttributes().isSoftDeleted()) {
                groupResponseList.add(groupDtoToResponse(group));
            }
        }
        return new GroupListResponse()
                .setGroups(groupResponseList)
                .setCode(0)
                .setPageTotal(groupResponseList.size());
    }

    public GroupListResponse createGroup(CreateGroupListRequest createGroupRequest, String accessToken) {

        HttpHeaders headers = getAuthHeaders(accessToken);
        GroupListResponse groupsResponse = new GroupListResponse();
        List<GroupResponse> groups = new ArrayList<>();
        UserDTO author = userService.getUserInfo(accessToken);

        for (CreateGroupRequest request : createGroupRequest.getGroups()) {
            GroupDTO groupDTO = mapCreateGroupRequestToGroupDTO(request, author);
            Group group = mapGroupDtoToGroup(groupDTO);

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

            if (request.getUsers() != null) {
                for (UserDTO user : request.getUsers()) {
                    userService.addUserToGroup(getGroupIgByName(accessToken, request.getGroupName()), user.getUserId(),
                            accessToken);
                }
            }

            GroupResponse response = groupDtoToResponse(groupDTO);
            groups.add(response);
        }

        return groupsResponse.setCode(0)
                .setGroups(groups)
                .setPageTotal(groups.size());
    }

    public CommonResponse editGroup(EditGroupListRequest editGroupsRequest, String accessToken) {

        return new CommonResponse();
    }

    public AccessTokenResponse deleteGroupById(DeleteGroupRequest deleteRequest, String accessToken) {

        AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
        HttpHeaders headers = getAuthHeaders(accessToken);
        for (String groupId : deleteRequest.getGroupIds()) {
            GroupDTO groupDTO = getGroupDTOById(groupId, accessToken);
            groupDTO.getAttributes().setSoftDeleted(true);
            Group group = mapGroupDtoToGroup(groupDTO);

            String url = createBaseUrl().pathSegment(groupId).toUriString();
            HttpEntity<Group> entity = new HttpEntity<>(group, headers);

            accessTokenResponse = restTemplate.exchange(url,
                    HttpMethod.PUT,
                    entity,
                    AccessTokenResponse.class).getBody();
        }
        return accessTokenResponse;
    }

    public BlockStatusGroupResponse changeBlockStatusGroup(String accessToken,
                                                           ChangeGroupStatusListRequest changeGroupStatusRequest) {
        List<Group> groups = new ArrayList<>();
        /**
         * TODO получить список групп из реквеста, поменять статусы(проверить возможность), заполнить ответ
         */

        return new BlockStatusGroupResponse();
    }

    public List<GroupDTO> findAllGroups(String accessToken) {
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
            groups.add(jsonObject.getString(keyId));
        }

        List<GroupDTO> groupList = new ArrayList<>();

        for (String groupId : groups) {
            GroupDTO groupDTO = getGroupDTOById(groupId, accessToken);
            groupList.add(groupDTO);
        }

        return groupList;
    }

    private GroupResponse groupDtoToResponse(GroupDTO groupDTO) {

        return new GroupResponse()
                .setGroupId(groupDTO.getId())
                .setGroupName(groupDTO.getName())
                .setPolicies(groupDTO.getAttributes().getPolicies())
                .setUsers(groupDTO.getUsers())
                .setGroupAdmin(groupDTO.getAttributes().getGroupAdmin())
                .setGroupAuditor(groupDTO.getAttributes().getGroupAuditor())
                .setPriority(groupDTO.getAttributes().getPriority())
                .setBlocked(groupDTO.getAttributes().isBlocked())
                .setSoftDeleted(groupDTO.getAttributes().isSoftDeleted())
                .setCreatedAt(groupDTO.getAttributes().getCreatedAt())
                .setCreatedBy(groupDTO.getAttributes().getCreatedBy().getUserName())
                .setUpdateAt(groupDTO.getAttributes().getUpdatedAt())
                .setUpdateBy(groupDTO.getAttributes().getUpdatedBy().getUserName());

    }

    private GroupDTO getGroupDTOById(String groupId, String accessToken) {
        HttpHeaders headers = getAuthHeaders(accessToken);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);
        String url = createBaseUrl().pathSegment(groupId).toUriString();

        String stringResponse = restTemplate.exchange(url,
                HttpMethod.GET,
                entity,
                String.class).getBody();

        if (stringResponse == null) {
            throw new FailedGetListOfGroupsException();
        }
        JSONObject groupAsJson = new JSONObject(stringResponse);

        return getGroupDTOFromJson(groupAsJson, accessToken);
    }

    private GroupDTO mapCreateGroupRequestToGroupDTO(CreateGroupRequest request, UserDTO author){
        LocalDateTime currentDateTime = LocalDateTime.now();
        AttributeDTO attributeDTO = new AttributeDTO()
                .setPriority(request.getPriority())
                .setPolicies(request.getPolicies())
                .setGroupAdmin(request.getGroupAdmin())
                .setGroupAuditor(request.getGroupAuditor())
                .setBlocked(false)
                .setSoftDeleted(false)
                .setCreatedAt(currentDateTime)
                .setCreatedBy(author)
                .setUpdatedAt(currentDateTime)
                .setUpdatedBy(author);

        return new GroupDTO()
                .setName(request.getGroupName())
                .setUsers(request.getUsers())
                .setAttributes(attributeDTO);

    }

    private Group mapGroupDtoToGroup(GroupDTO groupDTO) {
        Group group = new Group();
        AttributeDTO attributeDTO = groupDTO.getAttributes();
        Attribute attribute = new Attribute();
        long created = ZonedDateTime.of(attributeDTO.getCreatedAt(),
                ZoneId.systemDefault()).toInstant().toEpochMilli();
        long updated = ZonedDateTime.of(attributeDTO.getUpdatedAt(),
                ZoneId.systemDefault()).toInstant().toEpochMilli();


        String name = groupDTO.getName();
        List<String> priority = List.of(String.valueOf(attributeDTO.getPriority()));
        List<String> createdBy = List.of(attributeDTO.getCreatedBy().getUserName());
        List<String> createdAt = List.of(String.valueOf(created));
        List<String> updatedBy = List.of(attributeDTO.getUpdatedBy().getUserName());
        List<String> updatedAt = List.of(String.valueOf(updated));
        List<String> blocked = List.of(String.valueOf(groupDTO.getAttributes().isBlocked()));
        List<String> softDeleted = List.of(String.valueOf(groupDTO.getAttributes().isSoftDeleted()));
        List<String> groupAdmin = new ArrayList<>();
        List<String> groupAuditor = new ArrayList<>();
        List<String> policies = new ArrayList<>();

        for (PolicyDTO p : attributeDTO.getPolicies()) {
            String policy = p.getPolicyId() + " : " + p.getPolicyName();
            policies.add(policy);
        }

        for (GroupAdminDTO admin : attributeDTO.getGroupAdmin()) {
            String strAdmin = admin.getGroupAdminId() + " : " + admin.getGroupAdminName();
            groupAdmin.add(strAdmin);
        }

        for (GroupAuditorDTO auditor : attributeDTO.getGroupAuditor()) {
            String strAuditor = auditor.getGroupAuditorId() + " : " + auditor.getGroupAuditorName();
            groupAuditor.add(strAuditor);
        }

        attribute.setPriority(priority)
                .setPolicies(policies)
                .setGroupAdmin(groupAdmin)
                .setGroupAuditor(groupAuditor)
                .setBlocked(blocked)
                .setSoftDeleted(softDeleted)
                .setCreatedAt(createdAt)
                .setCreatedBy(createdBy)
                .setUpdatedAt(updatedAt)
                .setUpdatedBy(updatedBy);

        return group.setName(name).setAttributes(attribute);
    }

    private GroupDTO getGroupDTOFromJson(JSONObject groupFromKeycloak, String accessToken) {
        GroupDTO groupDTO = new GroupDTO();

        String groupId = groupFromKeycloak.getString(keyId);
        String groupName = groupFromKeycloak.getString(keyName);
        List<UserDTO> users = getGroupMembersByGroupId(accessToken, groupId);
        Attribute attribute = mapJsonToGroupAttributes(groupFromKeycloak);
        AttributeDTO attributeDTO = attributeToAttributeDTO(attribute, accessToken);

        groupDTO.setId(groupId).setName(groupName).setUsers(users).setAttributes(attributeDTO);

        return groupDTO;
    }

    private List<UserDTO> getGroupMembersByGroupId(String accessToken, String groupId) {
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
            if (rawGroup.getString(keyName).equals(name)) {
                groupId = rawGroup.getString(keyId);
            }
        }

        return groupId;
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

    private AttributeDTO attributeToAttributeDTO(Attribute attribute, String accessToken) {
        AttributeDTO attributeDTO = new AttributeDTO();

        int priority = Integer.parseInt(attribute.getPriority().get(0));
        boolean blocked = Boolean.parseBoolean(attribute.getBlocked().get(0));
        boolean softDeleted = Boolean.parseBoolean(attribute.getSoftDeleted().get(0));
        UserDTO createdBy = new UserDTO().setUserName(attribute.getCreatedBy().get(0));
        UserDTO updatedBy = new UserDTO().setUserName(attribute.getUpdatedBy().get(0));
        String ctime = attribute.getCreatedAt().get(0);
        LocalDateTime createdAt =
                Instant.ofEpochMilli(Long.parseLong(ctime)).atZone(ZoneId.systemDefault()).toLocalDateTime();
        String utime = attribute.getUpdatedAt().get(0);
        LocalDateTime updatedAt =
                Instant.ofEpochMilli(Long.parseLong(utime)).atZone(ZoneId.systemDefault()).toLocalDateTime();

        List<String> policyFromKeycloak = attribute.getPolicies();
        List<PolicyDTO> policyDTOList = new ArrayList<>();
        for (String policy : policyFromKeycloak) {
            PolicyDTO policyDTO = new PolicyDTO();
            policyDTO.setPolicyId(policy.split(" : ")[0]);
            policyDTO.setPolicyName(policy.split(" : ")[1]);
            policyDTOList.add(policyDTO);
        }

        List<String> groupAdminFromKeycloak = attribute.getGroupAdmin();
        List<GroupAdminDTO> groupAdmins = new ArrayList<>();
        for (String s : groupAdminFromKeycloak) {
            UserDTO userDTO = userService.userToUserDTO(s.split(" : ")[0], accessToken);
            groupAdmins.add(new GroupAdminDTO()
                    .setGroupAdminId(userDTO.getUserId())
                    .setGroupAdminName(userDTO.getUserName()));
        }

        List<String> groupAuditorsFromKeycloak = attribute.getGroupAuditor();
        List<GroupAuditorDTO> groupAuditors = new ArrayList<>();
        for (String s : groupAuditorsFromKeycloak) {
            UserDTO userDTO = userService.userToUserDTO(s.split(" : ")[0], accessToken);
            groupAuditors.add(new GroupAuditorDTO()
                    .setGroupAuditorId(userDTO.getUserId())
                    .setGroupAuditorName(userDTO.getUserName()));
        }

        return attributeDTO
                .setPolicies(policyDTOList)
                .setGroupAdmin(groupAdmins)
                .setGroupAuditor(groupAuditors)
                .setBlocked(blocked)
                .setSoftDeleted(softDeleted)
                .setPriority(priority)
                .setCreatedBy(createdBy)
                .setCreatedAt(createdAt)
                .setUpdatedBy(updatedBy)
                .setUpdatedAt(updatedAt);
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

            if (attributesJson.has("createdBy")) {
                JSONArray arrayCreatedBy = attributesJson.getJSONArray("createdBy");
                groupAttribute.setCreatedBy(List.of(arrayCreatedBy.getString(0)));
            }

            if (attributesJson.has("createdAt")) {
                JSONArray arrayCreatedAt = attributesJson.getJSONArray("createdAt");
                String date = arrayCreatedAt.getString(0);
                groupAttribute.setCreatedAt(List.of(date));
            }

            if (attributesJson.has("updatedBy")) {
                JSONArray arrayUpdatedBy = attributesJson.getJSONArray("updatedBy");
                groupAttribute.setUpdatedBy(List.of(arrayUpdatedBy.getString(0)));
            }

            if (attributesJson.has("updatedAt")) {
                JSONArray arrayUpdatedAt = attributesJson.getJSONArray("updatedAt");
                String date = arrayUpdatedAt.getString(0);
                groupAttribute.setUpdatedAt(List.of(date));
            }

            JSONArray arrayBlocked = attributesJson.getJSONArray("blocked");
            groupAttribute.setBlocked(List.of(arrayBlocked.getString(0)));

            JSONArray arraySoftDeleted = attributesJson.getJSONArray("softDeleted");
            groupAttribute.setSoftDeleted(List.of(arraySoftDeleted.getString(0)));

            JSONArray arrayPriority = attributesJson.getJSONArray("priority");
            groupAttribute.setPriority(List.of(arrayPriority.getString(0)));

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
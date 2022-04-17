package ru.maruchekas.keycloak.controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.maruchekas.keycloak.AbstractTest;
import ru.maruchekas.keycloak.api.request.*;
import ru.maruchekas.keycloak.config.Constants;
import ru.maruchekas.keycloak.dto.GroupAdminDTO;
import ru.maruchekas.keycloak.dto.GroupAuditorDTO;
import ru.maruchekas.keycloak.dto.PolicyDTO;
import ru.maruchekas.keycloak.dto.UserDTO;

import java.util.List;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class GroupControllerTest extends AbstractTest {

    @Autowired
    private MockMvc mockMvc;

    private String accessToken;

    private String username;
    private String password;
    private String clientId;
    private final CreateGroupListRequest createGroupListRequest = new CreateGroupListRequest();
    private final CreateGroupRequest createGroupRequest = new CreateGroupRequest();
    private final EditGroupListRequest editGroupListRequest = new EditGroupListRequest();
    private final EditGroupRequest editGroupRequest = new EditGroupRequest();
    private String groupName;
    private List<UserDTO> users;
    private List<PolicyDTO> policies;
    private List<GroupAdminDTO> groupAdmin;
    private List<GroupAuditorDTO> groupAuditor;
    private int priority;
    private static String groupId;

    @BeforeEach
    public void setupData() throws Exception {
        username = "myUser";
        password = "password";
        clientId = "mitra-client";
        groupName = "testGroup";
        users = List.of(new UserDTO()
                .setUserId("a871cda0-64ee-45bb-b94e-4f2f128ee632")
                .setUserName("testuser")
                .setUserEmail("test@mail.net"));
        policies = List.of(new PolicyDTO()
                .setPolicyId("0191e84a-2111-4c85-b1f5-c86ac063e1r8")
                .setPolicyName("fictional"));
        groupAdmin = List.of(new GroupAdminDTO()
                .setGroupAdminId("a871cda0-64ee-45bb-b94e-4f2f128ee632")
                .setGroupAdminName("testuser"));
        groupAuditor = List.of(new GroupAuditorDTO()
                .setGroupAuditorId("a871cda0-64ee-45bb-b94e-4f2f128ee632")
                .setGroupAuditorName("testuser"));
        priority = 22;

        createGroupRequest
                .setGroupName(groupName)
                .setPriority(priority)
                .setUsers(users)
                .setPolicies(policies)
                .setGroupAdmin(groupAdmin)
                .setGroupAuditor(groupAuditor);
        createGroupListRequest.setGroups(List.of(createGroupRequest));

        editGroupRequest
                .setGroupId(groupId)
                .setGroupName(groupName + "_updated")
                .setPolicies(policies)
                .setGroupAdmin(groupAdmin)
                .setGroupAuditor(groupAuditor)
                .setPriority(33);
        editGroupListRequest.setGroups(List.of(editGroupRequest));

        getAccessToken();
    }

    @AfterEach
    public void cleanup() {
    }

    public void getAccessToken() throws Exception {
        RefreshTokenRequest tokenRequest = new RefreshTokenRequest();
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(username);
        authRequest.setPassword(password);
        authRequest.setClientId(clientId);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(authRequest))
                .accept(MediaType.APPLICATION_JSON)).andReturn();

        JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
        tokenRequest.setRefreshToken(jsonObject.getString("access_token"));
        accessToken = "Bearer " + jsonObject.getString("access_token");
    }

    @Test
    @Order(10)
    public void createGroupTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/groups/add-group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .content(mapper.writeValueAsString(createGroupListRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(0))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @Order(20)
    public void createDuplicateGroupTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/groups/add-group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .content(mapper.writeValueAsString(createGroupListRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage")
                        .value(Constants.ELEMENT_ALREADY_EXISTS.getMessage()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @Order(30)
    public void getAllGroupsTest() throws Exception {
        FilterRequest filter = new FilterRequest();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .content(mapper.writeValueAsString(filter))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(0))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        JSONObject jsonResult = new JSONObject(result.getResponse().getContentAsString());
        JSONArray groups = jsonResult.getJSONArray("groups");
        groupId = groups.getJSONObject(0).getString("groupId");
        System.out.println(groupId);

    }

    @Test
    @Order(40)
    public void getGroupByIdTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/groups/{group-id}", groupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    @Order(50)
    public void getNotExistedGroupByIdTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/groups/{id}", groupId + "a")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage")
                        .value(Constants.ELEMENT_NOT_FOUND.getMessage()))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

    }

    @Test
    @Order(60)
    public void updateGroupByIdTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/groups/edit-group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .content(mapper.writeValueAsString(editGroupListRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    @Order(70)
    public void blockGroupByIdTest() throws Exception {

        GroupStatusChangeRequest changeStatusRequest = new GroupStatusChangeRequest();
        changeStatusRequest.setGroupId(List.of(groupId));
        changeStatusRequest.setBlocked(true);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/groups/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .content(mapper.writeValueAsString(changeStatusRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    @Order(80)
    public void unblockGroupByIdTest() throws Exception {

        GroupStatusChangeRequest changeStatusRequest = new GroupStatusChangeRequest();
        changeStatusRequest.setGroupId(List.of(groupId));
        changeStatusRequest.setBlocked(false);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/groups/unblock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .content(mapper.writeValueAsString(changeStatusRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    @Order(90)
    public void deleteGroupByIdTest() throws Exception {

        DeleteGroupRequest deleteRequest = new DeleteGroupRequest().setGroupIds(List.of(groupId));

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .content(mapper.writeValueAsString(deleteRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    @Order(100)
    public void getDeletedGroupByIdTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/groups/{group-id}", groupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage")
                        .value(Constants.GROUP_NOT_FOUND.getMessage()))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

    }

    @Test
    @Order(110)
    public void blockDeletedGroupByIdTest() throws Exception {

        GroupStatusChangeRequest changeStatusRequest = new GroupStatusChangeRequest();
        changeStatusRequest.setGroupId(List.of(groupId));
        changeStatusRequest.setBlocked(true);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/groups/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .content(mapper.writeValueAsString(changeStatusRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage")
                        .value(Constants.GROUP_NOT_FOUND.getMessage()))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

    }

    @Test
    @Order(120)
    public void updateDeletedGroupByIdTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/groups/edit-group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .content(mapper.writeValueAsString(editGroupListRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage")
                        .value(Constants.GROUP_NOT_FOUND.getMessage()))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

    }

    @Test
    public void updateNotExistedGroupByIdTest() throws Exception {
        editGroupListRequest.getGroups().get(0).setGroupId("not-such-group-id-91ae56d7e7b5");

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/groups/edit-group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .content(mapper.writeValueAsString(editGroupListRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage")
                        .value(Constants.ELEMENT_NOT_FOUND.getMessage()))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

    }

    @Test
    public void deleteNotExistedGroupByIdTest() throws Exception {

        DeleteGroupRequest deleteRequest = new DeleteGroupRequest()
                .setGroupIds(List.of("not-such-group-id-91ae56d7e7b5"));

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .content(mapper.writeValueAsString(deleteRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage")
                        .value(Constants.ELEMENT_NOT_FOUND.getMessage()))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

    }

    @Test
    public void getAllGroupsWhenNotAuthorizedTest() throws Exception {

        String invalidAccessToken = accessToken + "a";
        FilterRequest filter = new FilterRequest();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", invalidAccessToken)
                        .content(mapper.writeValueAsString(filter))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage")
                        .value(Constants.INVALID_ACCESS_TOKEN.getMessage()))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

    }
}
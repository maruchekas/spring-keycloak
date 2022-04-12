package ru.maruchekas.keycloak.controller;

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
import ru.maruchekas.keycloak.api.request.AuthRequest;
import ru.maruchekas.keycloak.api.request.CreateGroupRequest;
import ru.maruchekas.keycloak.api.request.RefreshTokenRequest;
import ru.maruchekas.keycloak.config.Constants;

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
    private String groupId;

    @BeforeEach
    public void setupData() throws Exception {
        username = "myUser";
        password = "password";
        clientId = "mitra-client";
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
    @Order(1)
    public void createGroupTest() throws Exception {

        CreateGroupRequest createGroupRequest = new CreateGroupRequest("TestGroup");

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .content(mapper.writeValueAsString(createGroupRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

    }

    @Test
    @Order(2)
    public void getGroupByIdTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/groups/{id}", "a0acf9b4-392b-49b6-89be-7dadf193527a")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    @Order(2)
    public void getNotExistedGroupByIdTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/groups/{id}", "a0acf9b4-392b-49b6-89be-0000aaaa1111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value(Constants.ELEMENT_NOT_FOUND.getMessage()))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

    }

    @Test
    @Order(3)
    public void updateGroupByIdTest() throws Exception {

        CreateGroupRequest createGroupRequest = new CreateGroupRequest("TestGroupUpdated");

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/groups/{id}", groupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .content(mapper.writeValueAsString(createGroupRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    @Order(4)
    public void updateNotExistedGroupByIdTest() throws Exception {

        CreateGroupRequest createGroupRequest = new CreateGroupRequest("TestGroupUpdated");

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/groups/{id}", "31da63c9-527a-44e0-926b-2afab6829fc7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .content(mapper.writeValueAsString(createGroupRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value(Constants.ELEMENT_NOT_FOUND.getMessage()))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

    }

    @Test
    @Order(5)
    public void deleteGroupByIdTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/groups/{id}", groupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    @Order(6)
    public void deleteNotExistedGroupByIdTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/groups/{id}", "31da63c9-527a-44e0-926b-2afab6829fc7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value(Constants.ELEMENT_NOT_FOUND.getMessage()))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

    }

    @Test
    public void getAllGroupsTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    public void getAllGroupsWhenNotAuthorizedTest() throws Exception {

        String invalidAccessToken = accessToken + "a";

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", invalidAccessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value(Constants.INVALID_LOGIN_PASSWORD.getMessage()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());

    }

    @Test
    public void getRolesTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/groups/{id}/roles", "b78c037c-cd74-4a49-af86-d635f587f70e")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

    }
}

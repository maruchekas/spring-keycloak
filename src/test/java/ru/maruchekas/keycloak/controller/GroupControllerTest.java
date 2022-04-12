package ru.maruchekas.keycloak.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
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
@AutoConfigureMockMvc
public class GroupControllerTest extends AbstractTest {

    @Autowired
    private MockMvc mockMvc;

    private String accessToken;

    private String username;
    private String password;
    private String clientId;

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
    public void createGroupTest() throws Exception {

        CreateGroupRequest createGroupRequest = new CreateGroupRequest("TestGroup");

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .content(mapper.writeValueAsString(createGroupRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    public void updateGroupByIdTest() throws Exception {

        CreateGroupRequest createGroupRequest = new CreateGroupRequest("TestGroupUpdated");

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/groups/{id}", "31da63c9-527a-44e0-926b-2afab6829fc7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .content(mapper.writeValueAsString(createGroupRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
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
    public void deleteGroupByIdTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/groups/{id}", "31da63c9-527a-44e0-926b-2afab6829fc7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
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
        RefreshTokenRequest tokenRequest = new RefreshTokenRequest();
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(username);
        authRequest.setPassword(password);
        authRequest.setClientId(clientId);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(authRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        JSONObject jsonObject = new JSONObject(result.getResponse().getContentAsString());
        tokenRequest.setRefreshToken(jsonObject.getString("access_token"));
        System.out.println(jsonObject.getString("access_token"));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jsonObject.getString("access_token"))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());


    }
}

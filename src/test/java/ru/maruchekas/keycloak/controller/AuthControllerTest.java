package ru.maruchekas.keycloak.controller;

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
import ru.maruchekas.keycloak.api.request.RefreshTokenRequest;
import ru.maruchekas.keycloak.config.Constants;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(value = {"classpath:application-test.yml"})
public class AuthControllerTest extends AbstractTest {

    @Autowired
    private MockMvc mockMvc;

    private final RefreshTokenRequest refreshToken = new RefreshTokenRequest();
    private String username;
    private String password;
    private String clientId;

    @BeforeEach
    public void setupData() throws Exception {
        username = "myUser";
        password = "password";
        clientId = "mitra-client";
        getToken();
    }


    @AfterEach
    public void cleanup() {
    }

    public void getToken() throws Exception {

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
        refreshToken.setRefreshToken(jsonObject.getString("refresh_token"));
    }

    @Test
    public void loginTest() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(username);
        authRequest.setPassword(password);
        authRequest.setClientId(clientId);
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(authRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void badLoginTest() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("myUser1");
        authRequest.setPassword(password);
        authRequest.setClientId(clientId);
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(authRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage")
                        .value(Constants.INVALID_ACCESS_TOKEN.getMessage()))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void logoutTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/authenticate/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(refreshToken))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void refreshTokenTest() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/authenticate/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(refreshToken))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void badRefreshTokenTest() throws Exception {

        RefreshTokenRequest invalidToken = refreshToken.setRefreshToken(refreshToken.getRefreshToken() + "a");

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/authenticate/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidToken))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage")
                        .value(Constants.INVALID_REFRESH_TOKEN.getMessage()))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void badLogoutTest() throws Exception {

        RefreshTokenRequest invalidToken = refreshToken.setRefreshToken(refreshToken.getRefreshToken() + "a");

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/authenticate/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidToken))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage")
                        .value(Constants.INVALID_REFRESH_TOKEN.getMessage()))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

}

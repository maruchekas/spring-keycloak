package ru.maruchekas.keycloak.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.maruchekas.keycloak.AbstractTest;
import ru.maruchekas.keycloak.api.request.AuthRequest;
import ru.maruchekas.keycloak.api.request.RefreshTokenRequest;
import ru.maruchekas.keycloak.config.Constants;

import java.util.Random;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(value = {"classpath:application-test.properties"})
public class AuthControllerTest extends AbstractTest {

    @Autowired
    private MockMvc mockMvc;

    private String username;
    private String password;
    private String clientId;
    private String realm;

    @BeforeEach
    public void setup() {
        super.setup();
        username = "myUser";
        password = "password";
        clientId = "mitra-client";
        realm = "MitraSoftRealm";
    }

    @AfterEach
    public void cleanup() {
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
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value(Constants.INVALID_LOGIN_PASSWORD.getMessage()))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void logoutTest() throws Exception {
        RefreshTokenRequest tokenRequest = new RefreshTokenRequest();
        tokenRequest.setRefreshToken(new Random().ints(1, 255)
                .limit(128)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString());

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/authenticate/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(tokenRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value(Constants.INVALID_REFRESH_TOKEN.getMessage()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

}

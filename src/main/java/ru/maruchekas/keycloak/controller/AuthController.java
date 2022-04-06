package ru.maruchekas.keycloak.controller;

import lombok.RequiredArgsConstructor;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.maruchekas.keycloak.api.request.AuthRequest;
import ru.maruchekas.keycloak.service.KeycloakClientService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/authenticate")
public class AuthController {

    private final KeycloakClientService keycloakClientService;

    @PostMapping
    public ResponseEntity<AccessTokenResponse> authenticate(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(keycloakClientService.authenticate(request));
    }
}

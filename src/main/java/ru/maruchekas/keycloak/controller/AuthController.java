package ru.maruchekas.keycloak.controller;

import lombok.RequiredArgsConstructor;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.maruchekas.keycloak.api.request.AuthRequest;
import ru.maruchekas.keycloak.api.request.RefreshTokenRequest;
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

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refresh(@RequestBody RefreshTokenRequest refreshToken) {
        return ResponseEntity.ok(keycloakClientService.refreshToken(refreshToken));
    }

    @PostMapping(path = "/logout")
    public ResponseEntity<AccessTokenResponse> logout(@RequestBody RefreshTokenRequest refreshToken) {
        return ResponseEntity.ok(keycloakClientService.logout(refreshToken));
    }
}

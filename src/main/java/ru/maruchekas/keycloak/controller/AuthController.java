package ru.maruchekas.keycloak.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.maruchekas.keycloak.api.request.AuthRequest;
import ru.maruchekas.keycloak.api.request.RefreshTokenRequest;
import ru.maruchekas.keycloak.exception.AuthenticationDataException;
import ru.maruchekas.keycloak.exception.InvalidTokenException;
import ru.maruchekas.keycloak.service.KeycloakClientService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Контроллер для работы с Keycloak")
@RequestMapping("/api/authenticate")
public class AuthController {

    private final KeycloakClientService keycloakClientService;

    @Operation(summary = "Аутентификация клиента на сервере")
    @PostMapping
    public ResponseEntity<AccessTokenResponse> authenticate(@RequestBody AuthRequest request)
            throws AuthenticationDataException {
        return ResponseEntity.ok(keycloakClientService.authenticate(request));
    }

    @Operation(summary = "Метод обновления токена")
    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refresh(@RequestBody RefreshTokenRequest refreshToken)
            throws InvalidTokenException {
        return ResponseEntity.ok(keycloakClientService.refreshToken(refreshToken));
    }

    @Operation(summary = "Метод завершения сессии")
    @PostMapping(path = "/logout")
    public ResponseEntity<AccessTokenResponse> logout(@RequestBody RefreshTokenRequest refreshToken)
            throws InvalidTokenException {
        return ResponseEntity.ok(keycloakClientService.logout(refreshToken));
    }
}

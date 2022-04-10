package ru.maruchekas.keycloak.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.maruchekas.keycloak.api.request.AuthRequest;
import ru.maruchekas.keycloak.api.request.RefreshTokenRequest;
import ru.maruchekas.keycloak.exception.AuthenticationDataException;
import ru.maruchekas.keycloak.exception.InvalidTokenException;
import ru.maruchekas.keycloak.service.AuthService;

import java.util.HashMap;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Контроллер для работы с доступом клиента")
@RequestMapping("/api/authenticate")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Аутентификация клиента на сервере")
    @PostMapping
    public ResponseEntity<AccessTokenResponse> authenticate(@RequestBody AuthRequest request)
            throws AuthenticationDataException {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @Operation(summary = "Метод обновления токена")
    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refresh(@RequestBody RefreshTokenRequest refreshToken)
            throws InvalidTokenException {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    @Operation(summary = "Метод завершения сессии")
    @PostMapping(path = "/logout")
    public ResponseEntity<AccessTokenResponse> logout(@RequestBody RefreshTokenRequest refreshToken)
            throws InvalidTokenException {
        return ResponseEntity.ok(authService.logout(refreshToken));
    }
}

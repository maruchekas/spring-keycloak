package ru.maruchekas.keycloak.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum Constants {

    INVALID_REFRESH_TOKEN("invalid or expired refresh token"),
    INVALID_CLIENT("Invalid client credentials"),
    INVALID_LOGIN_PASSWORD("Invalid login or password"),
    AUTHENTICATION_ERROR("Authentication error"),
    ELEMENT_NOT_FOUND("Could not find such element");

    private final String message;

}

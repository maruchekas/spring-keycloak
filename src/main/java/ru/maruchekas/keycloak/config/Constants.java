package ru.maruchekas.keycloak.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum Constants {

    INVALID_REFRESH_TOKEN("invalid or expired refresh token"),
    AUTHENTICATION_ERROR("authentication error");

    private final String message;

}

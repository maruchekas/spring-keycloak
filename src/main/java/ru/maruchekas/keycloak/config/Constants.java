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
    ELEMENT_NOT_FOUND("Could not find such element"),
    ELEMENT_ALREADY_EXISTS("Such element already exists"),
    FAILED_GET_GROUP("Failed to get a group"),
    FAILED_GET_MEMBERS("Failed to get members"),
    FAILED_GET_GROUP_LIST("Failed to get a list of groups"),
    FAILED_CREATE_GROUP("Failed to create a group");

    private final String message;

}

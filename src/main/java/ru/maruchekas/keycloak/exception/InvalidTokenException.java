package ru.maruchekas.keycloak.exception;

import ru.maruchekas.keycloak.config.Constants;

public class InvalidTokenException extends Exception{
    public InvalidTokenException() {
        super(Constants.INVALID_REFRESH_TOKEN.getMessage());
    }
}
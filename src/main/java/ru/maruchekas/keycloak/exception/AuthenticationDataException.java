package ru.maruchekas.keycloak.exception;

import ru.maruchekas.keycloak.config.Constants;

public class AuthenticationDataException extends Exception{
    public AuthenticationDataException() {
        super(Constants.AUTHENTICATION_ERROR.getMessage());
    }
}

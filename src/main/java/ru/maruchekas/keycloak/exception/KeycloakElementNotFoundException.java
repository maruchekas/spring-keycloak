package ru.maruchekas.keycloak.exception;

import ru.maruchekas.keycloak.config.Constants;

public class KeycloakElementNotFoundException extends Exception{
    public KeycloakElementNotFoundException() {
        super(Constants.ELEMENT_NOT_FOUND.getMessage());
    }
}

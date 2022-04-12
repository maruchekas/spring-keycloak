package ru.maruchekas.keycloak.exception;

import ru.maruchekas.keycloak.config.Constants;

public class GroupAlreadyExistsException extends RuntimeException {
    public GroupAlreadyExistsException() {
        super(Constants.ELEMENT_ALREADY_EXISTS.getMessage());
    }
}

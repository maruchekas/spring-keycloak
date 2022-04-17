package ru.maruchekas.keycloak.exception;

import ru.maruchekas.keycloak.config.Constants;

public class GroupNotFoundException extends RuntimeException {
    public GroupNotFoundException() {
        super(Constants.GROUP_NOT_FOUND.getMessage());
    }
}

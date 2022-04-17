package ru.maruchekas.keycloak.exception;

import ru.maruchekas.keycloak.config.Constants;

public class FailedGetMembersException extends RuntimeException {
    public FailedGetMembersException() {
        super(Constants.FAILED_GET_USER.getMessage());
    }
}

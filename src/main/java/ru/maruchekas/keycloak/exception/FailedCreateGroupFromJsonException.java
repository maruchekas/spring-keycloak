package ru.maruchekas.keycloak.exception;

import ru.maruchekas.keycloak.config.Constants;

public class FailedCreateGroupFromJsonException extends RuntimeException {
    public FailedCreateGroupFromJsonException() {
        super(Constants.FAILED_CREATE_GROUP.getMessage());
    }
}

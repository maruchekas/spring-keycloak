package ru.maruchekas.keycloak.exception;

import ru.maruchekas.keycloak.config.Constants;

public class FailedCreateGroupException extends RuntimeException {
    public FailedCreateGroupException() {
        super(Constants.FAILED_CREATE_GROUP.getMessage());
    }
}

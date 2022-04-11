package ru.maruchekas.keycloak.exception;

import ru.maruchekas.keycloak.config.Constants;

public class FailedGetGroupFromJsonException extends RuntimeException {
    public FailedGetGroupFromJsonException() {
        super(Constants.FAILED_GET_GROUP.getMessage());
    }
}

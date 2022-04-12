package ru.maruchekas.keycloak.exception;

import ru.maruchekas.keycloak.config.Constants;

public class FailedGetListOfGroupsException extends RuntimeException {
    public FailedGetListOfGroupsException() {
        super(Constants.FAILED_GET_GROUP_LIST.getMessage());
    }
}

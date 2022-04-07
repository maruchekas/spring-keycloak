package ru.maruchekas.keycloak.api.response;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BadResponse {
    private String error;
    private String message;
}

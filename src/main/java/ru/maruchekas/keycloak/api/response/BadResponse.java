package ru.maruchekas.keycloak.api.response;

import lombok.*;
import lombok.experimental.Accessors;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class BadResponse {
    private String error;
    private String message;
}

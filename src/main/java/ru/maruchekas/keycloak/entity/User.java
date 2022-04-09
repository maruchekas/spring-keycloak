package ru.maruchekas.keycloak.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class User {

    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
}

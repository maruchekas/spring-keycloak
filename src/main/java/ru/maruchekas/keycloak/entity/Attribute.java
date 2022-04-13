package ru.maruchekas.keycloak.entity;

import lombok.Data;

import java.util.List;

@Data
public class Attribute {

    private List<String> policies;
    private List<String> groupAdmin;
    private List<String> groupAuditor;

}

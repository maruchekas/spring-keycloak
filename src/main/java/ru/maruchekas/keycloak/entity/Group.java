package ru.maruchekas.keycloak.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
public class Group {

    private String id;
    private String name;
    private String path;
    private LocalDateTime createdBy;
    private boolean blocked;
    private Attribute attributes;
    private List<String> realmRoles;
    private List<String> clientRoles;
    private List<Group> subGroups;
    private Access access;

}


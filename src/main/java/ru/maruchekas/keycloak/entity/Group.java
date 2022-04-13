package ru.maruchekas.keycloak.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
public class Group {

    private String groupId;
    private String groupName;
    private LocalDateTime createdBy;
    private boolean blocked;
    private boolean softDeleted;
    private Attribute attributes;
    private List<String> realmRoles;
    private List<String> clientRoles;
    private List<Group> subGroups;

}


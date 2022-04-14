package ru.maruchekas.keycloak.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GroupAuditor {

    private String groupAdminId;
    private String groupAdminName;

}

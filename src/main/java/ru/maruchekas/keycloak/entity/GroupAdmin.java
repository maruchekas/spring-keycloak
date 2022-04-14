package ru.maruchekas.keycloak.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GroupAdmin {

    private String groupAdminId;
    private String groupAdminName;

}

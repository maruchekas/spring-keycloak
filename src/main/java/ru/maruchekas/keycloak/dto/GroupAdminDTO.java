package ru.maruchekas.keycloak.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GroupAdminDTO {

    private String groupAdminId;
    private String groupAdminName;

}

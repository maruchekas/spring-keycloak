package ru.maruchekas.keycloak.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GroupAuditorDTO {

    private String groupAuditorId;
    private String groupAuditorName;

}

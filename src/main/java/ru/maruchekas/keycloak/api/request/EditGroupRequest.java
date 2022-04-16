package ru.maruchekas.keycloak.api.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.maruchekas.keycloak.dto.PolicyDTO;
import ru.maruchekas.keycloak.dto.GroupAdminDTO;
import ru.maruchekas.keycloak.dto.GroupAuditorDTO;
import ru.maruchekas.keycloak.entity.User;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class EditGroupRequest {

    private String groupId;
    private String groupName;
    private List<User> users;
    private List<PolicyDTO> policies;
    private List<GroupAdminDTO> groupAdminDTO;
    private List<GroupAuditorDTO> groupAuditorDTO;
    private boolean softDeleted;
    private int priority;
}
package ru.maruchekas.keycloak.api.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.maruchekas.keycloak.dto.GroupAdminDTO;
import ru.maruchekas.keycloak.dto.GroupAuditorDTO;
import ru.maruchekas.keycloak.dto.PolicyDTO;
import ru.maruchekas.keycloak.dto.UserDTO;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CreateGroupRequest {

    private String groupId;
    private String groupName;
    private List<UserDTO> users;
    private List<PolicyDTO> policies;
    private List<GroupAdminDTO> groupAdmin;
    private List<GroupAuditorDTO> groupAuditor;
    private int priority;
}

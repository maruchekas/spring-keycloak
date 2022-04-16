package ru.maruchekas.keycloak.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.maruchekas.keycloak.dto.PolicyDTO;
import ru.maruchekas.keycloak.dto.UserDTO;
import ru.maruchekas.keycloak.dto.GroupAdminDTO;
import ru.maruchekas.keycloak.dto.GroupAuditorDTO;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupResponse {

    private Integer code;
    private String groupId;
    private String groupName;
    private List<UserDTO> users;
    private List<PolicyDTO> policies;
    private List<GroupAdminDTO> groupAdmin;
    private List<GroupAuditorDTO> groupAuditor;
    private int priority;
    private boolean blocked;
    private boolean softDeleted;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updateAt;
    private String updateBy;

}
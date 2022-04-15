package ru.maruchekas.keycloak.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.maruchekas.keycloak.dto.PolicyDTO;
import ru.maruchekas.keycloak.entity.GroupAdmin;
import ru.maruchekas.keycloak.entity.GroupAuditor;
import ru.maruchekas.keycloak.entity.Policy;
import ru.maruchekas.keycloak.entity.User;

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
    private List<User> users;
    private List<PolicyDTO> policies;
    private List<GroupAdmin> groupAdmin;
    private List<GroupAuditor> groupAuditor;
    private int priority;
    private boolean blocked;
    private boolean softDeleted;
    private LocalDateTime createdAt;
    private String createdBy;

}

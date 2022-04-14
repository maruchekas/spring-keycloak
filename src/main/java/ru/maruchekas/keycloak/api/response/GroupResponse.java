package ru.maruchekas.keycloak.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
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
public class GroupResponse {

    private int code;
    private String groupName;
    private List<User> users;
    private List<Policy> policies;
    private List<GroupAdmin> groupAdmin;
    private List<GroupAuditor> groupAuditor;
    private int priority;
    private boolean blocked;
    private boolean softDeleted;
    private LocalDateTime createdAt;
    private String createdBy;

}

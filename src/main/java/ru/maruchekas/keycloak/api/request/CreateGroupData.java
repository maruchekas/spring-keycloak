package ru.maruchekas.keycloak.api.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.maruchekas.keycloak.entity.*;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CreateGroupData {

    private String groupName;
    private List<User> users;
    private List<Policy> policies;
    private List<GroupAdmin> groupAdmin;
    private List<GroupAuditor> groupAuditor;
    private int priority;
}

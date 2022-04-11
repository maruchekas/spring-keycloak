package ru.maruchekas.keycloak.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.maruchekas.keycloak.entity.Access;
import ru.maruchekas.keycloak.entity.Group;
import ru.maruchekas.keycloak.entity.Policy;

import java.time.LocalDateTime;
import java.util.List;

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
public class GroupDTO {

        private String id;
        private String name;
        private String path;
        private LocalDateTime createDateTime;
        private boolean blocked;
        private List<UserDTO> users;
        private List<UserDTO> groupAdmin;
        private List<UserDTO> groupAuditor;
        private List<Policy> policies;
        private List<String> realmRoles;
        private List<String> clientRoles;
        private List<Group> subGroups;
        private Access access;


}

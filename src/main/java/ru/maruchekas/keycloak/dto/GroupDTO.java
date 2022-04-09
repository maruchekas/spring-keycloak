package ru.maruchekas.keycloak.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.maruchekas.keycloak.entity.Access;
import ru.maruchekas.keycloak.entity.Group;

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
        private List<UserDTO> users;
        private List<String> realmRoles;
        private List<String> clientRoles;
        private List<Group> subGroups;
        private Access access;


}

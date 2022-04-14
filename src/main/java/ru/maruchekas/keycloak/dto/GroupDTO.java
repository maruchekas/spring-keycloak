package ru.maruchekas.keycloak.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
public class GroupDTO {

        private String code;
        private String groupId;
        private String groupName;
        private List<UserDTO> users;
        private List<String> policies;
        private List<String> groupAdmin;
        private List<String> groupAuditor;
        private boolean blocked;
        private boolean softDeleted;
        private int priority;
        private String createdAt;
        private String createdBy;
        private String updatedAt;
        private String updatedBy;

}

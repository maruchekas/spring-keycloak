package ru.maruchekas.keycloak.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.maruchekas.keycloak.entity.Policy;

import java.time.LocalDateTime;
import java.util.List;

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
public class GroupDTO {

        private int code;
        private String groupId;
        private String groupName;
        private List<UserDTO> users;
        private List<String> policies;
        private List<String> groupAdmin;
        private List<String> groupAuditor;
        private boolean blocked;
        private boolean softDeleted;
        private LocalDateTime createdAt;
        private String createdBy;
        private LocalDateTime updatedAt;
        private UserDTO updatedBy;

}

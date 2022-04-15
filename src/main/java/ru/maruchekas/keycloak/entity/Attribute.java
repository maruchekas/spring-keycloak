package ru.maruchekas.keycloak.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.maruchekas.keycloak.dto.UserDTO;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Attribute {

    private List<String> policies;
    private List<String> groupAdmin;
    private List<String> groupAuditor;
    private boolean blocked;
    private boolean softDeleted;
    private int priority;
    private LocalDateTime createdAt;
    private UserDTO createdBy;
    private LocalDateTime updatedAt;
    private UserDTO updatedBy;
}

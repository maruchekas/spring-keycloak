package ru.maruchekas.keycloak.entity;

import lombok.*;
import lombok.experimental.Accessors;

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
    private List<String> createdAt;
    private List<String> createdBy;
    private LocalDateTime updatedAt;
//    private List<String> updatedBy;
//    private boolean blocked;
//    private boolean softDeleted;

}

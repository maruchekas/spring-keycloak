package ru.maruchekas.keycloak.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttributeDTO {

    private List<PolicyDTO> policies;
    private List<GroupAdminDTO> groupAdmin;
    private List<GroupAuditorDTO> groupAuditor;
    private boolean blocked;
    private boolean softDeleted;
    private LocalDateTime createdAt;
    private UserDTO createdBy;
    private LocalDateTime updatedAt;
    private UserDTO updatedBy;
    private int priority;
}
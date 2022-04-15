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
public class AttributeDTO {

    private List<String> priority;
    private List<String> groupAdmin;
    private List<String> groupAuditor;
    private List<String> createdAt;
    private List<String> createdBy;
    private List<String> blocked;
    private List<String> softDeleted;
    private List<String> policies;

//    private List<String> updatedAt;
//    private List<String> updatedBy;

}

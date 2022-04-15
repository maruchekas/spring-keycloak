package ru.maruchekas.keycloak.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.Accessors;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Policy {

    private String id;
    private String name;
    private String containerId;
    private boolean composite;
    private boolean clientRole;

}

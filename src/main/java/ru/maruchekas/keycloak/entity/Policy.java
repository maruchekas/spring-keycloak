package ru.maruchekas.keycloak.entity;

import lombok.*;
import lombok.experimental.Accessors;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Policy {

    private String policyId;
    private String policyName;

}

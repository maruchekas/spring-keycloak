package ru.maruchekas.keycloak.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
public class GroupDTO {

        private String id;
        private String name;
        private AttributeDTO attributes;

}

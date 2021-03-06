package ru.maruchekas.keycloak.api.response;

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
public class BlockStatusGroupResponse {

    private String code;
    private String errorUid;
    private String errorMessage;
}

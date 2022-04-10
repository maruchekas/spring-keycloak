package ru.maruchekas.keycloak.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Access {

    private boolean view;
    private boolean manage;
    private boolean manageMembership;

}

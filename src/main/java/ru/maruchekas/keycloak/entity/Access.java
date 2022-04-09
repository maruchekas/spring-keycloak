package ru.maruchekas.keycloak.entity;

import lombok.Data;

@Data
public class Access {

    private boolean view;
    private boolean manage;
    private boolean manageMembership;

}

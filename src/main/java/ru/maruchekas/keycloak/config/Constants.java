package ru.maruchekas.keycloak.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum Constants {

    CREATE_GROUP_ERR("86JW48U2"),
    NOT_FOUND_ERR("64KG92B7"),
    GET_GROUP_ERR("77SM28Y3"),
    ACC_TOKEN_ER("82NY16E1"),
    REF_TOKEN_ER("67HB68J7"),
    INVALID_REFRESH_TOKEN("invalid or expired refresh token"),
    INVALID_ACCESS_TOKEN("invalid or expired token"),
    INVALID_CLIENT("Invalid client credentials"),
    INVALID_LOGIN_PASSWORD("Invalid login or password"),
    AUTHENTICATION_ERROR("Authentication error"),
    ELEMENT_NOT_FOUND("Could not find such element"),
    ELEMENT_ALREADY_EXISTS("Such element already exists"),
    FAILED_GET_GROUP("Failed to get a group"),
    FAILED_GET_MEMBERS("Failed to get members"),
    FAILED_GET_GROUP_LIST("Failed to get a list of groups"),
    FAILED_CREATE_GROUP("Failed to create a group. One or more fields are filled in incorrectly");

    private final String message;

}

//        11QM60D3
//        49NU98P7
//        48JZ65S6
//        68FC26G1
//        48OI65I4
//        84CH10J2
//        94BL56J5
//        14OA43P4
//        22WD67M8
//        36YD51P2
//        14JC58P2
//        79VQ35J0
//        84XH32X2
//        51OJ26N4
//        83SS92P1




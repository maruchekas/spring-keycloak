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
    ALREADY_EXISTS_ERR("11QM60D3"),
    GET_GROUP_ERR("77SM28Y3"),
    GET_USER_ERR("49NU98P7"),
    ACC_TOKEN_ERR("82NY16E1"),
    AUTHENTICATE_ERR("48JZ65S6"),
    REF_TOKEN_ERR("67HB68J7"),
    INVALID_REFRESH_TOKEN("invalid or expired refresh token"),
    INVALID_ACCESS_TOKEN("invalid or expired token"),
    INVALID_CLIENT("Invalid client credentials"),
    INVALID_LOGIN_PASSWORD("Invalid login or password"),
    AUTHENTICATION_ERROR("Authentication error"),
    ELEMENT_NOT_FOUND("Could not find such element"),
    GROUP_NOT_FOUND("Group not found"),
    ELEMENT_ALREADY_EXISTS("Such element already exists"),
    FAILED_GET_GROUP("Failed to get a group"),
    FAILED_GET_USER("Failed to get user from group"),
    FAILED_GET_GROUP_LIST("Failed to get a list of groups"),
    FAILED_CREATE_GROUP("Failed to create a group. One or more fields are filled in incorrectly");

    private final String message;

}

//
//
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




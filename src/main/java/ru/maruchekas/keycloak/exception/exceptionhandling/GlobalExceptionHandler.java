package ru.maruchekas.keycloak.exception.exceptionhandling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import ru.maruchekas.keycloak.api.response.CommonResponse;
import ru.maruchekas.keycloak.config.Constants;
import ru.maruchekas.keycloak.exception.*;

import java.util.UUID;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    ResponseEntity<CommonResponse> handleInvalidTokenException(InvalidTokenException exception) {
        CommonResponse badDataResponse = new CommonResponse().setErrorUid("token").setErrorMessage(exception.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler
    ResponseEntity<CommonResponse> handleAuthenticationErrorException(AuthenticationDataException exception) {
        CommonResponse badDataResponse = new CommonResponse().setErrorUid("authenticate").setErrorMessage(exception.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler
    ResponseEntity<CommonResponse> handleInvalidClientException(HttpClientErrorException.Unauthorized exception) {
        CommonResponse badDataResponse = new CommonResponse()
                .setCode(401)
                .setErrorUid(UUID.randomUUID().toString())
                .setErrorMessage(Constants.INVALID_ACCESS_TOKEN.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler
    ResponseEntity<CommonResponse> handleInvalidLoginPasswordException(HttpClientErrorException.Forbidden exception) {
        CommonResponse badDataResponse = new CommonResponse()
                .setErrorUid("access denied")
                .setErrorMessage(Constants.INVALID_LOGIN_PASSWORD.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler
    ResponseEntity<CommonResponse> handleElementNotFoundException(HttpClientErrorException.NotFound exception) {
        CommonResponse badDataResponse = new CommonResponse()
                .setErrorUid("not found")
                .setErrorMessage(Constants.ELEMENT_NOT_FOUND.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    ResponseEntity<CommonResponse> handleGroupAlreadyExistsException(GroupAlreadyExistsException exception) {
        CommonResponse badDataResponse = new CommonResponse()
                .setCode(400)
                .setErrorUid(UUID.randomUUID().toString())
                .setErrorMessage(Constants.ELEMENT_ALREADY_EXISTS.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    ResponseEntity<CommonResponse> handleFailedGetListOfGroupsException(FailedGetListOfGroupsException exception) {
        CommonResponse badDataResponse = new CommonResponse()
                .setErrorUid("conflict")
                .setErrorMessage(Constants.FAILED_GET_GROUP_LIST.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    ResponseEntity<CommonResponse> handleFailedGetGroupException(FailedGetGroupFromJsonException exception) {
        CommonResponse badDataResponse = new CommonResponse()
                .setErrorUid("getting_group")
                .setErrorMessage(Constants.FAILED_GET_GROUP.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    ResponseEntity<CommonResponse> handleFailedGetMembersException(FailedGetMembersException exception) {
        CommonResponse badDataResponse = new CommonResponse()
                .setErrorUid("getting_members")
                .setErrorMessage(Constants.FAILED_GET_MEMBERS.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    ResponseEntity<CommonResponse> handleFailedCreateGroupException(FailedCreateGroupException exception) {
        CommonResponse badDataResponse = new CommonResponse()
                .setCode(400)
                .setErrorUid(Constants.CREATE_GROUP_ERR.getMessage())
                .setErrorMessage(Constants.FAILED_CREATE_GROUP.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.BAD_REQUEST);
    }
}
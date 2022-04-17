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
        CommonResponse badDataResponse = new CommonResponse()
                .setCode(401)
                .setErrorUid(Constants.INVALID_ACCESS_TOKEN.getMessage())
                .setErrorMessage(exception.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler
    ResponseEntity<CommonResponse> handleAuthenticationErrorException(AuthenticationDataException exception) {
        CommonResponse badDataResponse = new CommonResponse()
                .setCode(401)
                .setErrorUid(Constants.AUTHENTICATION_ERROR.getMessage())
                .setErrorMessage(exception.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler
    ResponseEntity<CommonResponse> handleInvalidClientException(HttpClientErrorException.Unauthorized exception) {
        CommonResponse badDataResponse = new CommonResponse()
                .setCode(401)
                .setErrorUid(Constants.ACC_TOKEN_ERR.getMessage())
                .setErrorMessage(Constants.INVALID_ACCESS_TOKEN.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler
    ResponseEntity<CommonResponse> handleInvalidLoginPasswordException(HttpClientErrorException.Forbidden exception) {
        CommonResponse badDataResponse = new CommonResponse()
                .setCode(401)
                .setErrorUid(Constants.AUTHENTICATE_ERR.getMessage())
                .setErrorMessage(Constants.INVALID_LOGIN_PASSWORD.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler
    ResponseEntity<CommonResponse> handleElementNotFoundException(HttpClientErrorException.NotFound exception) {
        CommonResponse badDataResponse = new CommonResponse()
                .setCode(404)
                .setErrorUid(Constants.NOT_FOUND_ERR.getMessage())
                .setErrorMessage(Constants.ELEMENT_NOT_FOUND.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    ResponseEntity<CommonResponse> handleGroupNotFoundException(GroupNotFoundException exception) {
        CommonResponse badDataResponse = new CommonResponse()
                .setCode(404)
                .setErrorUid(Constants.NOT_FOUND_ERR.getMessage())
                .setErrorMessage(Constants.GROUP_NOT_FOUND.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    ResponseEntity<CommonResponse> handleGroupAlreadyExistsException(GroupAlreadyExistsException exception) {
        CommonResponse badDataResponse = new CommonResponse()
                .setCode(400)
                .setErrorUid(Constants.ALREADY_EXISTS_ERR.getMessage())
                .setErrorMessage(Constants.ELEMENT_ALREADY_EXISTS.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    ResponseEntity<CommonResponse> handleFailedGetListOfGroupsException(FailedGetListOfGroupsException exception) {
        CommonResponse badDataResponse = new CommonResponse()
                .setCode(400)
                .setErrorUid(Constants.GET_GROUP_ERR.getMessage())
                .setErrorMessage(Constants.FAILED_GET_GROUP_LIST.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    ResponseEntity<CommonResponse> handleFailedGetGroupException(FailedGetGroupFromJsonException exception) {
        CommonResponse badDataResponse = new CommonResponse()
                .setCode(400)
                .setErrorUid(Constants.GET_GROUP_ERR.getMessage())
                .setErrorMessage(Constants.FAILED_GET_GROUP.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    ResponseEntity<CommonResponse> handleFailedGetMembersException(FailedGetMembersException exception) {
        CommonResponse badDataResponse = new CommonResponse()
                .setCode(404)
                .setErrorUid(Constants.GET_USER_ERR.getMessage())
                .setErrorMessage(Constants.FAILED_GET_USER.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.NOT_FOUND);
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
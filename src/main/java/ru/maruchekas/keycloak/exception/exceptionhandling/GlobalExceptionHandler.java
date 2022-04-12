package ru.maruchekas.keycloak.exception.exceptionhandling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import ru.maruchekas.keycloak.api.response.BadResponse;
import ru.maruchekas.keycloak.config.Constants;
import ru.maruchekas.keycloak.exception.*;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    ResponseEntity<BadResponse> handleInvalidTokenException(InvalidTokenException exception) {
        BadResponse badDataResponse = new BadResponse().setError("token").setMessage(exception.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    ResponseEntity<BadResponse> handleAuthenticationErrorException(AuthenticationDataException exception) {
        BadResponse badDataResponse = new BadResponse().setError("authenticate").setMessage(exception.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler
    ResponseEntity<BadResponse> handleInvalidClientException(HttpClientErrorException.BadRequest exception) {
        BadResponse badDataResponse = new BadResponse()
                .setError("invalid client")
                .setMessage(Constants.INVALID_CLIENT.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler
    ResponseEntity<BadResponse> handleInvalidLoginPasswordException(HttpClientErrorException.Unauthorized exception) {
        BadResponse badDataResponse = new BadResponse()
                .setError("access denied")
                .setMessage(Constants.INVALID_LOGIN_PASSWORD.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler
    ResponseEntity<BadResponse> handleElementNotFoundException(HttpClientErrorException.NotFound exception) {
        BadResponse badDataResponse = new BadResponse()
                .setError("not found")
                .setMessage(Constants.ELEMENT_NOT_FOUND.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    ResponseEntity<BadResponse> handleGroupAlreadyExistsException(GroupAlreadyExistsException exception) {
        BadResponse badDataResponse = new BadResponse()
                .setError("duplicate")
                .setMessage(Constants.ELEMENT_ALREADY_EXISTS.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    ResponseEntity<BadResponse> handleFailedGetListOfGroupsException(FailedGetListOfGroupsException exception) {
        BadResponse badDataResponse = new BadResponse()
                .setError("conflict")
                .setMessage(Constants.FAILED_GET_GROUP_LIST.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    ResponseEntity<BadResponse> handleFailedGetGroupException(FailedGetGroupFromJsonException exception) {
        BadResponse badDataResponse = new BadResponse()
                .setError("getting_group")
                .setMessage(Constants.FAILED_GET_GROUP.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    ResponseEntity<BadResponse> handleFailedGetMembersException(FailedGetMembersException exception) {
        BadResponse badDataResponse = new BadResponse()
                .setError("getting_members")
                .setMessage(Constants.FAILED_GET_MEMBERS.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    ResponseEntity<BadResponse> handleFailedCreateGroupException(FailedCreateGroupFromJsonException exception) {
        BadResponse badDataResponse = new BadResponse()
                .setError("creating_group")
                .setMessage(Constants.FAILED_CREATE_GROUP.getMessage());
        return new ResponseEntity<>(badDataResponse, HttpStatus.BAD_REQUEST);
    }
}

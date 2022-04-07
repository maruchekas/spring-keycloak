package ru.maruchekas.keycloak.exception.exceptionhandling;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.maruchekas.keycloak.api.response.BadResponse;
import ru.maruchekas.keycloak.exception.AuthenticationDataException;
import ru.maruchekas.keycloak.exception.InvalidTokenException;

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
}

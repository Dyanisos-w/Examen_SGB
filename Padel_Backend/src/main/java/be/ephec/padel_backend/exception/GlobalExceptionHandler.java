package be.ephec.padel_backend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoIdProvidedException.class)
    public ResponseEntity<GenericError> handleNoIdProvidedException(NoIdProvidedException e) {
        return ResponseEntity.badRequest().body(new GenericError(1, "No ID provided"));
    }
}

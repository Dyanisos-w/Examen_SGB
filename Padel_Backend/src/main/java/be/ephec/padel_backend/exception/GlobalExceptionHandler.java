package be.ephec.padel_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoIdProvidedException.class)
    public ResponseEntity<GenericError> handleNoIdProvidedException(NoIdProvidedException e) {
        return ResponseEntity.badRequest().body(new GenericError(1, "No ID provided", e.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex) {
        return buildResponse(ex.getError(), ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ReservationException.class)
    public ResponseEntity<Object> handleReservationException(ReservationException ex) {
        return buildResponse(ex.getError(), ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<Object> handlePaymentException(PaymentException ex) {
        return buildResponse(ex.getError(), ex.getMessage(), HttpStatus.PAYMENT_REQUIRED);
    }

    private ResponseEntity<Object> buildResponse(String errorType, String message, HttpStatus status) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", errorType);
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now());
        return new ResponseEntity<>(body, status);
    }
}

package be.ephec.padel_backend.exception;


import java.time.LocalDateTime;

public record GenericError(int code, String error, String message, LocalDateTime timestamp) {
}

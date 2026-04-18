package be.ephec.padel_backend.exception;

import org.springframework.security.core.userdetails.User;

public class UnauthorizedAccessException extends RuntimeException {
    private final User user;

    public UnauthorizedAccessException(User user, String message) {
        super(message);
        this.user = user;
    }
}

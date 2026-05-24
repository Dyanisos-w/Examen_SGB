package be.ephec.padel_backend.exception;

public class AuthenticationException extends RuntimeException {
    private final String error = "LOGIN_ERROR";

    public AuthenticationException(String message) {
        super(message);
    }

    public String getError() {
        return error;
    }
}


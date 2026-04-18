package be.ephec.padel_backend.exception;

public class NoFirstNameProvidedException extends RuntimeException {
    public NoFirstNameProvidedException(String message) {
        super(message);
    }
}

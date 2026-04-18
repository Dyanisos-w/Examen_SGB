package be.ephec.padel_backend.exception;

public class NoIdProvidedException extends RuntimeException {
    public NoIdProvidedException(String message) {
        super(message);
    }
}

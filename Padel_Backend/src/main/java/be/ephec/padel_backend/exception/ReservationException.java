package be.ephec.padel_backend.exception;

public class ReservationException extends RuntimeException {
    private final String error = "RESERVATION_ERROR";

    public ReservationException(String message) {
        super(message);
    }

    public String getError() {
        return error;
    }
}


package be.ephec.padel_backend.exception;

public class PaymentException extends RuntimeException {
    private final String error = "PAYMENT_ERROR";

    public PaymentException(String message) {
        super(message);
    }

    public String getError() {
        return error;
    }
}


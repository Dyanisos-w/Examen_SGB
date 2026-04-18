package be.ephec.padel_backend.exception;

import java.util.UUID;

public class ObjectNotFoundException extends RuntimeException {

    private final UUID id;

    public ObjectNotFoundException( UUID id) {
        super("Object %s not found".formatted(id));
        this.id = id;
    }

    public ObjectNotFoundException(UUID id, Throwable cause) {
        super("Object %s not found".formatted(id), cause);
        this.id = id;
    }

    public UUID id() {
        return id;
    }
}

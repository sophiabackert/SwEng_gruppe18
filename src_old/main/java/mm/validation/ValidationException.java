package mm.validation;

import java.util.List;
import java.util.ArrayList;

/**
 * Exception für Validierungsfehler bei Level-JSON-Dateien.
 */
public class ValidationException extends Exception {
    private final List<String> messages;

    public ValidationException(List<String> messages) {
        super(String.join("\n", messages));
        this.messages = new ArrayList<>(messages);
    }

    /**
     * Erstellt eine neue ValidationException mit einer Fehlermeldung.
     * @param message Die Fehlermeldung
     */
    public ValidationException(String message) {
        super(message);
        this.messages = List.of(message);
    }

    /**
     * Erstellt eine neue ValidationException mit einer Fehlermeldung und einer Ursache.
     * @param message Die Fehlermeldung
     * @param cause Die Ursache
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.messages = List.of(message);
    }

    /**
     * Gets all validation error messages.
     *
     * @return List of validation error messages
     */
    public List<String> getAllMessages() {
        return new ArrayList<>(messages);
    }
} 
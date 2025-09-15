package mm.exceptions;

/**
 * Exception für Fehler beim Speichern von Levels.
 */
public class LevelSaveException extends Exception {
    public LevelSaveException(String message) {
        super(message);
    }

    public LevelSaveException(String message, Throwable cause) {
        super(message, cause);
    }
} 
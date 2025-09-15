package mm.exceptions;

/**
 * Exception für Fehler beim Laden von Levels.
 */
public class LevelLoadException extends Exception {
    public LevelLoadException(String message) {
        super(message);
    }

    public LevelLoadException(String message, Throwable cause) {
        super(message, cause);
    }
} 
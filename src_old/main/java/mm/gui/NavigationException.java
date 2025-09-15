package mm.gui;

/**
 * Exception-Klasse für Navigationsfehler.
 */
public class NavigationException extends Exception {
    public NavigationException(String message) {
        super(message);
    }
    public NavigationException(String message, Throwable cause) {
        super(message, cause);
    }
}
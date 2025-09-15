package mm.validation;

/**
 * Exception für Level-Validierungsfehler.
 */
public class LevelValidationException extends ValidationException {
    private final ErrorType errorType;
    
    /**
     * Typen von Validierungsfehlern.
     */
    public enum ErrorType {
        INVALID_JSON("Ungültiges JSON-Format"),
        MISSING_REQUIRED_FIELD("Pflichtfeld fehlt"),
        INVALID_FIELD_VALUE("Ungültiger Feldwert"),
        INVALID_LEVEL_STRUCTURE("Ungültige Level-Struktur"),
        INVALID_THUMBNAIL("Ungültiges Thumbnail"),
        FILE_ACCESS_ERROR("Dateizugriffsfehler"),
        UNKNOWN_ERROR("Unbekannter Fehler");
        
        private final String message;
        
        ErrorType(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    /**
     * Erstellt eine neue LevelValidationException.
     * @param errorType Der Typ des Validierungsfehlers
     * @param details Detaillierte Fehlerbeschreibung
     */
    public LevelValidationException(ErrorType errorType, String details) {
        super(formatMessage(errorType, details));
        this.errorType = errorType;
    }
    
    /**
     * Gibt den Typ des Validierungsfehlers zurück.
     * @return Der Fehlertyp
     */
    public ErrorType getErrorType() {
        return errorType;
    }
    
    private static String formatMessage(ErrorType errorType, String details) {
        return String.format("%s: %s", errorType.getMessage(), details);
    }
} 
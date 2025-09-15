package mm.domain.storage;

/**
 * Enum für die Schwierigkeitsgrade eines Levels.
 * <p>
 * Unterstützt die Werte EASY, MEDIUM, HARD und CUSTOM.
 * </p>
 */
public enum Difficulty {
    /** Einfacher Schwierigkeitsgrad */
    EASY, 
    /** Mittlerer Schwierigkeitsgrad */
    MEDIUM, 
    /** Hoher Schwierigkeitsgrad */
    HARD, 
    /** Benutzerdefinierter Schwierigkeitsgrad */
    CUSTOM;

    /**
     * Wandelt einen String in das passende Difficulty-Enum um.
     * @param difficulty Schwierigkeitsgrad als String
     * @return Das passende Enum
     * @throws IllegalArgumentException bei ungültigem Wert
     */
    public static Difficulty fromString(String difficulty) {
        return Difficulty.valueOf(difficulty.toUpperCase());
    }
}
package mm.model;

/**
 * Enthält die Metadaten eines Levels.
 */
public class LevelInfo {
    private final String fileName;
    private final String name;
    private final String difficulty;
    private final String description;
    private final boolean isCustomLevel;
    
    /**
     * Erstellt ein neues LevelInfo-Objekt.
     * @param fileName Der Dateiname des Levels
     * @param name Der Name des Levels
     * @param difficulty Der Schwierigkeitsgrad des Levels
     * @param description Die Beschreibung des Levels
     * @param isCustomLevel Ob es sich um ein benutzerdefiniertes Level handelt
     */
    public LevelInfo(String fileName, String name, String difficulty, String description, boolean isCustomLevel) {
        this.fileName = fileName;
        this.name = name;
        this.difficulty = difficulty;
        this.description = description;
        this.isCustomLevel = isCustomLevel;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDifficulty() {
        return difficulty;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isCustomLevel() {
        return isCustomLevel;
    }
} 
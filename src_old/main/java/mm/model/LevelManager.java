package mm.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import mm.validation.LevelSchemaValidator;
import mm.validation.ValidationException;
import mm.validation.LevelValidationException;
import mm.validation.LevelValidationException.ErrorType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Verwaltet das Laden, Speichern und Löschen von Levels.
 */
public class LevelManager {
    private static final String LEVELS_DIRECTORY = "src/main/resources/levels";
    private static final String BACKGROUNDS_DIRECTORY = "src/main/resources/assets/backgrounds";
    private static final String CUSTOM_LEVEL_PREFIX = "custom_";
    
    private final LevelSchemaValidator validator;
    private final Gson gson;
    
    public LevelManager() {
        this.validator = new LevelSchemaValidator();
        this.gson = new Gson();
    }
    
    /**
     * Lädt alle verfügbaren Levels.
     * @return Eine Liste aller verfügbaren Levels
     * @throws LevelValidationException wenn ein kritischer Fehler beim Laden auftritt
     */
    public List<LevelInfo> loadLevels() throws LevelValidationException {
        List<LevelInfo> levels = new ArrayList<>();
        File levelsDir = new File(LEVELS_DIRECTORY);
        
        if (!levelsDir.exists() || !levelsDir.isDirectory()) {
            throw new LevelValidationException(ErrorType.FILE_ACCESS_ERROR, 
                "Level-Verzeichnis nicht gefunden: " + LEVELS_DIRECTORY);
        }
        
        File[] levelFiles = levelsDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (levelFiles == null) {
            throw new LevelValidationException(ErrorType.FILE_ACCESS_ERROR, 
                "Fehler beim Lesen des Level-Verzeichnisses");
        }
        
        for (File levelFile : levelFiles) {
            try {
                LevelInfo levelInfo = loadLevelInfo(levelFile);
                levels.add(levelInfo);
            } catch (LevelValidationException e) {
                // Log den Fehler, aber fahre mit dem nächsten Level fort
                System.err.println("Fehler beim Laden des Levels " + levelFile.getName() + ": " + e.getMessage());
            }
        }
        
        return levels;
    }
    
    /**
     * Lädt die Metadaten eines einzelnen Levels.
     * @param levelFile Die Level-Datei
     * @return Die Level-Informationen
     * @throws LevelValidationException wenn das Level ungültig ist
     */
    private LevelInfo loadLevelInfo(File levelFile) throws LevelValidationException {
        try {
            String content = Files.readString(levelFile.toPath());
            JsonObject json = validateAndParseJson(content);
            
            String fileName = levelFile.getName();
            boolean isCustomLevel = fileName.startsWith(CUSTOM_LEVEL_PREFIX);
            
            return new LevelInfo(
                fileName,
                extractString(json, "name", "Unbenanntes Level"),
                extractString(json, "difficulty", "medium"),
                extractString(json, "description", "Keine Beschreibung verfügbar"),
                isCustomLevel
            );
            
        } catch (IOException e) {
            throw new LevelValidationException(ErrorType.FILE_ACCESS_ERROR,
                "Fehler beim Lesen der Level-Datei: " + e.getMessage());
        }
    }
    
    /**
     * Importiert ein neues Level.
     * @param file Die zu importierende Level-Datei
     * @throws LevelValidationException wenn das Level ungültig ist
     * @throws IOException wenn ein Dateizugriffsfehler auftritt
     */
    public void importLevel(File file) throws LevelValidationException, IOException {
        // Validiere das Level-Format
        String content = Files.readString(file.toPath());
        JsonObject json = validateAndParseJson(content);
        
        // Validiere die Level-Struktur
        validateLevelStructure(json);
        
        // Kopiere die Level-Datei
        String newFileName = CUSTOM_LEVEL_PREFIX + file.getName();
        Path targetPath = Path.of(LEVELS_DIRECTORY, newFileName);
        Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        // Versuche das Thumbnail zu kopieren, falls vorhanden
        tryImportThumbnail(file);
    }
    
    /**
     * Löscht ein Level.
     * @param levelName Der Name des zu löschenden Levels
     * @param deleteThumbnail Ob auch das Thumbnail gelöscht werden soll
     * @throws IOException wenn ein Dateizugriffsfehler auftritt
     * @throws LevelValidationException wenn das Level nicht gelöscht werden kann
     */
    public void deleteLevel(String levelName, boolean deleteThumbnail) throws IOException, LevelValidationException {
        if (!levelName.startsWith(CUSTOM_LEVEL_PREFIX)) {
            throw new LevelValidationException(ErrorType.INVALID_FIELD_VALUE,
                "Nur importierte Levels können gelöscht werden");
        }
        
        Path levelPath = Path.of(LEVELS_DIRECTORY, levelName);
        if (!Files.deleteIfExists(levelPath)) {
            throw new LevelValidationException(ErrorType.FILE_ACCESS_ERROR,
                "Level-Datei konnte nicht gefunden werden: " + levelName);
        }
        
        if (deleteThumbnail) {
            String thumbnailName = levelName.replace(".json", ".png");
            Path thumbnailPath = Path.of(BACKGROUNDS_DIRECTORY, thumbnailName);
            Files.deleteIfExists(thumbnailPath);
        }
    }
    
    /**
     * Formatiert einen Level-Namen für die Anzeige.
     * @param levelName Der zu formatierende Level-Name
     * @return Der formatierte Level-Name
     */
    public String formatLevelName(String levelName) {
        return levelName
            .replace(CUSTOM_LEVEL_PREFIX, "")
            .replace(".json", "")
            .replace("_", " ");
    }
    
    /**
     * Validiert und parst JSON-Inhalt.
     * @param content Der JSON-Inhalt
     * @return Das geparste JsonObject
     * @throws LevelValidationException wenn das JSON ungültig ist
     */
    private JsonObject validateAndParseJson(String content) throws LevelValidationException {
        try {
            validator.validate(content);
            return gson.fromJson(content, JsonObject.class);
        } catch (JsonSyntaxException e) {
            throw new LevelValidationException(ErrorType.INVALID_JSON,
                "Ungültiges JSON-Format: " + e.getMessage());
        } catch (ValidationException e) {
            throw new LevelValidationException(ErrorType.INVALID_LEVEL_STRUCTURE,
                "Level-Validierung fehlgeschlagen: " + e.getMessage());
        }
    }
    
    /**
     * Validiert die Level-Struktur.
     * @param json Das zu validierende JsonObject
     * @throws LevelValidationException wenn die Struktur ungültig ist
     */
    private void validateLevelStructure(JsonObject json) throws LevelValidationException {
        // Prüfe Pflichtfelder
        if (!json.has("name")) {
            throw new LevelValidationException(ErrorType.MISSING_REQUIRED_FIELD,
                "Das Level muss einen Namen haben");
        }
        
        // Prüfe Level-Elemente
        if (!json.has("elements") || !json.get("elements").isJsonArray()) {
            throw new LevelValidationException(ErrorType.INVALID_LEVEL_STRUCTURE,
                "Das Level muss ein 'elements'-Array enthalten");
        }
        
        JsonArray elements = json.getAsJsonArray("elements");
        if (elements.size() == 0) {
            throw new LevelValidationException(ErrorType.INVALID_LEVEL_STRUCTURE,
                "Das Level muss mindestens ein Element enthalten");
        }
        
        // Prüfe Schwierigkeitsgrad
        if (json.has("difficulty")) {
            String difficulty = json.get("difficulty").getAsString();
            if (!difficulty.matches("^(easy|medium|hard)$")) {
                throw new LevelValidationException(ErrorType.INVALID_FIELD_VALUE,
                    "Ungültiger Schwierigkeitsgrad: " + difficulty);
            }
        }
    }
    
    /**
     * Versucht ein Thumbnail zu importieren.
     * @param levelFile Die Level-Datei
     */
    private void tryImportThumbnail(File levelFile) {
        String baseName = levelFile.getName().replace(".json", "");
        File thumbnailFile = new File(levelFile.getParent(), baseName + ".png");
        
        if (thumbnailFile.exists()) {
            try {
                Path targetPath = Path.of(BACKGROUNDS_DIRECTORY, CUSTOM_LEVEL_PREFIX + thumbnailFile.getName());
                Files.copy(thumbnailFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                // Thumbnail-Import ist optional, also nur loggen
                System.err.println("Warnung: Thumbnail konnte nicht importiert werden: " + e.getMessage());
            }
        }
    }
    
    /**
     * Extrahiert einen String aus einem JsonObject.
     * @param json Das JsonObject
     * @param key Der Schlüssel
     * @param defaultValue Der Standardwert
     * @return Der extrahierte String oder der Standardwert
     */
    private String extractString(JsonObject json, String key, String defaultValue) {
        return json.has(key) ? json.get(key).getAsString() : defaultValue;
    }
} 
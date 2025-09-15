package mm.core.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mm.core.storage.LevelData;
import mm.core.config.ObjectConf;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Validiert Level-JSON-Dateien auf korrekte Struktur und Inhalte.
 */
public class LevelValidator {
    
    // Gültige skinIds für Objekte
    private static final String[] VALID_SKIN_IDS = {
        "tennisball", "billiardball", "bowlingball", "balloon",
        "bucket", "cratebox", "domino", "log", "plank",
        "gameball", "goalzone", "smallgear", "largegear", "drivechain", "paddle"
    };
    
    /**
     * Validiert eine Level-JSON-Datei.
     * @param file Die zu validierende Datei
     * @return true wenn die Datei ein gültiges Level-Format hat
     */
    public static boolean isValidLevelFile(File file) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(file);
            
            // Prüfe ob alle erforderlichen Felder vorhanden sind
            if (!hasRequiredFields(root)) {
                System.out.println("Level-Validierung fehlgeschlagen: Fehlende Pflichtfelder");
                return false;
            }
            
            // Prüfe difficulty
            String difficulty = root.get("difficulty").asText();
            if (!isValidDifficulty(difficulty)) {
                System.out.println("Level-Validierung fehlgeschlagen: Ungültige Schwierigkeit: " + difficulty);
                return false;
            }
            
            // Prüfe objects Array
            JsonNode objects = root.get("objects");
            if (!objects.isArray()) {
                System.out.println("Level-Validierung fehlgeschlagen: 'objects' ist kein Array");
                return false;
            }
            
            // Validiere jedes Objekt
            for (JsonNode obj : objects) {
                if (!isValidObject(obj)) {
                    return false;
                }
            }
            
            // Prüfe limits
            JsonNode limits = root.get("limits");
            if (!limits.isObject()) {
                System.out.println("Level-Validierung fehlgeschlagen: 'limits' ist kein Objekt");
                return false;
            }
            
            return true;
            
        } catch (IOException e) {
            System.out.println("Fehler beim Lesen der Level-Datei: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.out.println("Unerwarteter Fehler bei der Level-Validierung: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private static boolean hasRequiredFields(JsonNode root) {
        return root.has("name") && root.has("difficulty") && 
               root.has("objective") && root.has("objects") && 
               root.has("limits");
    }
    
    private static boolean isValidDifficulty(String difficulty) {
        return "EASY".equals(difficulty) || "MEDIUM".equals(difficulty) || "HARD".equals(difficulty);
    }
    
    private static boolean isValidObject(JsonNode obj) {
        // Prüfe Pflichtfelder für Objekte
        if (!obj.has("x") || !obj.has("y") || !obj.has("angle") || 
            !obj.has("static") || !obj.has("skinId")) {
            System.out.println("Level-Validierung fehlgeschlagen: Objekt fehlt Pflichtfelder");
            return false;
        }
        
        // Prüfe Datentypen
        if (!obj.get("x").isNumber() || !obj.get("y").isNumber() || 
            !obj.get("angle").isNumber() || !obj.get("static").isBoolean()) {
            System.out.println("Level-Validierung fehlgeschlagen: Objekt hat falsche Datentypen");
            return false;
        }
        
        // Prüfe skinId
        String skinId = obj.get("skinId").asText();
        if (!isValidSkinId(skinId)) {
            System.out.println("Level-Validierung fehlgeschlagen: Ungültige skinId: " + skinId);
            return false;
        }
        
        return true;
    }
    
    private static boolean isValidSkinId(String skinId) {
        for (String validId : VALID_SKIN_IDS) {
            if (validId.equals(skinId)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Lädt ein validiertes Level.
     * @param file Die zu ladende Datei
     * @return Das geladene LevelData-Objekt
     * @throws IOException wenn die Datei nicht gelesen werden kann
     * @throws IllegalArgumentException wenn die Datei kein gültiges Level ist
     */
    public static LevelData loadValidatedLevel(File file) throws IOException, IllegalArgumentException {
        if (!isValidLevelFile(file)) {
            throw new IllegalArgumentException("Die Datei enthält kein gültiges Level-Format");
        }
        
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(file, LevelData.class);
    }
} 
package mm.domain.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mm.domain.storage.LevelData;
import java.io.File;
import java.io.IOException;

/**
 * Dienstklasse zur Validierung und zum Laden von Leveldateien im JSON-Format.
 * <p>
 * Prüft, ob eine gegebene Datei ein gültiges Level-Format besitzt (Felder, Werte, Objekttypen)
 * und ermöglicht das sichere Laden von Leveldaten als LevelData-Objekt.
 * </p>
 */
public class LevelValidator {
    
    /** Liste aller gültigen Skin-IDs für Objekte im Spiel */
    private static final String[] VALID_SKIN_IDS = {
        "tennisball", "billiardball", "bowlingball", "balloon",
        "bucket", "cratebox", "domino", "log", "plank",
        "gameball", "goalzone", "restrictionzone"
    };
    
    /**
     * Prüft, ob die angegebene Datei ein gültiges Level-Format besitzt.
     * @param file Die zu prüfende Datei
     * @return true, wenn die Datei ein gültiges Level-Format hat, sonst false
     */
    public static boolean isValidLevelFile(File file) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(file);
            
            if (!hasRequiredFields(root)) {
                return false;
            }
            
            String difficulty = root.get("difficulty").asText();
            if (!isValidDifficulty(difficulty)) {
                return false;
            }
            
            JsonNode objects = root.get("objects");
            if (!objects.isArray()) {
                return false;
            }
            
            for (JsonNode obj : objects) {
                if (!isValidObject(obj)) {
                    return false;
                }
            }
            
            JsonNode limits = root.get("limits");
            if (!limits.isObject()) {
                return false;
            }
            
            return true;
            
        } catch (IOException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Prüft, ob alle erforderlichen Felder im JSON-Root vorhanden sind.
     * @param root Das JSON-Root-Objekt
     * @return true, wenn alle Felder vorhanden sind
     */
    private static boolean hasRequiredFields(JsonNode root) {
        return root.has("name") && root.has("difficulty") && 
               root.has("objective") && root.has("objects") && 
               root.has("limits");
    }
    
    /**
     * Prüft, ob der Schwierigkeitsgrad gültig ist.
     * @param difficulty Schwierigkeitsgrad als String
     * @return true, wenn gültig
     */
    private static boolean isValidDifficulty(String difficulty) {
        return "EASY".equals(difficulty) || "MEDIUM".equals(difficulty) || "HARD".equals(difficulty);
    }
    
    /**
     * Prüft, ob ein Objekt-Knoten im JSON gültig ist.
     * @param obj Das Objekt-JSON
     * @return true, wenn gültig
     */
    private static boolean isValidObject(JsonNode obj) {
        if (!obj.has("x") || !obj.has("y") || !obj.has("angle") || 
            !obj.has("static") || !obj.has("skinId")) {
            return false;
        }
        
        if (!obj.get("x").isNumber() || !obj.get("y").isNumber() || 
            !obj.get("angle").isNumber() || !obj.get("static").isBoolean()) {
            return false;
        }
        
        String skinId = obj.get("skinId").asText();
        if (!isValidSkinId(skinId)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Prüft, ob die Skin-ID gültig ist.
     * @param skinId Die zu prüfende Skin-ID
     * @return true, wenn gültig
     */
    private static boolean isValidSkinId(String skinId) {
        for (String validId : VALID_SKIN_IDS) {
            if (validId.equals(skinId)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Lädt ein validiertes Level aus einer Datei.
     * @param file Die Leveldatei
     * @return Das geladene LevelData-Objekt
     * @throws IOException bei Lesefehlern
     * @throws IllegalArgumentException wenn das Format ungültig ist
     */
    public static LevelData loadValidatedLevel(File file) throws IOException, IllegalArgumentException {
        if (!isValidLevelFile(file)) {
            throw new IllegalArgumentException("Die Datei enthält kein gültiges Level-Format");
        }
        
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(file, LevelData.class);
    }
} 
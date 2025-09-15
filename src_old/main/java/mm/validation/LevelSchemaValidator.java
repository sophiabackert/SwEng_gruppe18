package mm.validation;

import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;

/**
 * Validiert Level-JSON-Dateien gegen das erwartete Schema.
 */
public class LevelSchemaValidator {
    
    /**
     * Validiert den JSON-Inhalt eines Levels.
     * @param jsonContent Der zu validierende JSON-String
     * @throws ValidationException wenn die Validierung fehlschlägt
     */
    public void validate(String jsonContent) throws ValidationException {
        try {
            JsonObject level = JsonParser.parseString(jsonContent).getAsJsonObject();
            
            // Prüfe Pflichtfelder
            validateRequiredFields(level);
            
            // Prüfe Schwierigkeitsgrad
            validateDifficulty(level.get("difficulty").getAsString());
            
            // Prüfe Objekte
            validateObjects(level.get("objects").getAsJsonArray());
            
        } catch (Exception e) {
            throw new ValidationException("JSON-Validierung fehlgeschlagen: " + e.getMessage());
        }
    }
    
    /**
     * Prüft, ob alle erforderlichen Felder vorhanden sind.
     */
    private void validateRequiredFields(JsonObject level) throws ValidationException {
        String[] requiredFields = {"name", "difficulty", "description", "objects"};
        for (String field : requiredFields) {
            if (!level.has(field)) {
                throw new ValidationException("Pflichtfeld fehlt: " + field);
            }
        }
    }
    
    /**
     * Prüft, ob der Schwierigkeitsgrad gültig ist.
     */
    private void validateDifficulty(String difficulty) throws ValidationException {
        String[] validDifficulties = {"EASY", "MEDIUM", "HARD"};
        boolean isValid = false;
        for (String valid : validDifficulties) {
            if (valid.equals(difficulty)) {
                isValid = true;
                break;
            }
        }
        if (!isValid) {
            throw new ValidationException("Ungültiger Schwierigkeitsgrad: " + difficulty);
        }
    }
    
    /**
     * Prüft die Level-Objekte auf Gültigkeit.
     */
    private void validateObjects(JsonArray objects) throws ValidationException {
        if (objects.size() == 0) {
            throw new ValidationException("Level muss mindestens ein Objekt enthalten");
        }
        
        for (JsonElement element : objects) {
            JsonObject object = element.getAsJsonObject();
            validateObject(object);
        }
    }
    
    /**
     * Prüft ein einzelnes Level-Objekt.
     */
    private void validateObject(JsonObject object) throws ValidationException {
        // Prüfe Pflichtfelder des Objekts
        String[] requiredFields = {"type", "position", "properties"};
        for (String field : requiredFields) {
            if (!object.has(field)) {
                throw new ValidationException("Objekt-Pflichtfeld fehlt: " + field);
            }
        }
        
        // Prüfe Position
        JsonObject position = object.get("position").getAsJsonObject();
        if (!position.has("x") || !position.has("y")) {
            throw new ValidationException("Position muss x und y Koordinaten enthalten");
        }
        
        // Prüfe Properties
        JsonObject properties = object.get("properties").getAsJsonObject();
        String[] requiredProperties = {"density", "friction", "restitution"};
        for (String prop : requiredProperties) {
            if (!properties.has(prop)) {
                throw new ValidationException("Objekt-Eigenschaft fehlt: " + prop);
            }
        }
    }
} 
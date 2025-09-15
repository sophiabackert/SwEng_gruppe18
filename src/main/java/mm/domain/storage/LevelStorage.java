package mm.domain.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Dienstklasse für das Laden und Speichern von Leveldaten im JSON-Format.
 * <p>
 * Nutzt Jackson zur Serialisierung und Deserialisierung von LevelData-Objekten.
 * </p>
 */
public final class LevelStorage {

    /** Jackson-ObjectMapper für die (De-)Serialisierung */
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * Privater Konstruktor, um Instanziierung zu verhindern.
     */
    private LevelStorage() {}

    /**
     * Speichert ein LevelData-Objekt als JSON-Datei.
     * @param level Das zu speichernde Level
     * @param target Zielpfad der Datei
     * @throws IOException bei Schreibfehlern
     */
    public static void save(LevelData level, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        MAPPER.writeValue(target.toFile(), level);
    }

    /**
     * Lädt ein LevelData-Objekt aus einer JSON-Datei.
     * @param source Pfad zur Quelldatei
     * @return Das geladene LevelData-Objekt
     * @throws IOException bei Lesefehlern
     */
    public static LevelData load(Path source) throws IOException {
        return MAPPER.readValue(source.toFile(), LevelData.class);
    }
}
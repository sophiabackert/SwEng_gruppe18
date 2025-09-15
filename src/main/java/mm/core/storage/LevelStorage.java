package mm.core.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LevelStorage {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private LevelStorage() {}

    /** Speichert EIN Level (inkl. Metadaten + Objekte). */
    public static void save(LevelData level, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        MAPPER.writeValue(target.toFile(), level);
    }

    /** Lädt EIN Level. */
    public static LevelData load(Path source) throws IOException {
        return MAPPER.readValue(source.toFile(), LevelData.class);
    }
}
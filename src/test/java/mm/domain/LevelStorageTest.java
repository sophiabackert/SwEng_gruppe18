package mm.domain;

import mm.domain.config.TennisballConf;
import mm.domain.storage.Difficulty;
import mm.domain.storage.LevelData;
import mm.domain.storage.LevelStorage;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class LevelStorageTest {

    @Test
    void testSaveAndLoadLevelData() throws IOException {
        var obj = new TennisballConf(1,2,0,false);
        var limits = Map.of("tennisball", 3);
        LevelData data = new LevelData("TestLevel", Difficulty.MEDIUM, "Ziel", List.of(obj), limits);
        var tempFile = Files.createTempFile("level", ".json");
        try {
            LevelStorage.save(data, tempFile);
            LevelData loaded = LevelStorage.load(tempFile);
            assertEquals(data.getName(), loaded.getName());
            assertEquals(data.getDifficulty(), loaded.getDifficulty());
            assertEquals(data.getObjective(), loaded.getObjective());
            assertEquals(data.getObjects(), loaded.getObjects());
            assertEquals(data.getLimits(), loaded.getLimits());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testLoadNonexistentFileThrows() {
        var path = new java.io.File("nonexistent_file_123456.json").toPath();
        assertThrows(IOException.class, () -> LevelStorage.load(path));
    }
} 
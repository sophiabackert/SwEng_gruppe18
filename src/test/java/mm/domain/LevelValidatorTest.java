package mm.domain;

import mm.domain.json.LevelValidator;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class LevelValidatorTest {

    @Test
    void testIsValidLevelFileWithValidFile() throws IOException {
        String json = "{\n" +
                "  \"name\": \"Test\",\n" +
                "  \"difficulty\": \"EASY\",\n" +
                "  \"objective\": \"Ziel\",\n" +
                "  \"objects\": [\n" +
                "    {\"x\":1,\"y\":2,\"angle\":0,\"static\":false,\"skinId\":\"tennisball\"}\n" +
                "  ],\n" +
                "  \"limits\": {\"tennisball\":3}\n" +
                "}";
        File temp = File.createTempFile("level", ".json");
        try (FileWriter fw = new FileWriter(temp)) { fw.write(json); }
        assertTrue(LevelValidator.isValidLevelFile(temp));
        temp.delete();
    }

    @Test
    void testIsValidLevelFileWithInvalidFile() throws IOException {
        String json = "{\"invalid\":true}";
        File temp = File.createTempFile("level", ".json");
        try (FileWriter fw = new FileWriter(temp)) { fw.write(json); }
        assertFalse(LevelValidator.isValidLevelFile(temp));
        temp.delete();
    }

    @Test
    void testLoadValidatedLevelThrowsOnInvalid() throws IOException {
        String json = "{\"invalid\":true}";
        File temp = File.createTempFile("level", ".json");
        try (FileWriter fw = new FileWriter(temp)) { fw.write(json); }
        assertThrows(IllegalArgumentException.class, () -> LevelValidator.loadValidatedLevel(temp));
        temp.delete();
    }
} 
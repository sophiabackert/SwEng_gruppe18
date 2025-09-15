package mm.domain;

import mm.domain.config.TennisballConf;
import mm.domain.storage.Difficulty;
import mm.domain.storage.LevelData;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class LevelDataTest {

    @Test
    void testConstructorAndGetters() {
        var obj = new TennisballConf(1,2,0,false);
        var limits = Map.of("tennisball", 3);
        LevelData data = new LevelData("TestLevel", Difficulty.MEDIUM, "Ziel", List.of(obj), limits);
        assertEquals("TestLevel", data.getName());
        assertEquals(Difficulty.MEDIUM, data.getDifficulty());
        assertEquals("Ziel", data.getObjective());
        assertEquals(List.of(obj), data.getObjects());
        assertEquals(3, data.getLimits().get("tennisball"));
    }

    @Test
    void testEmptyConstructorDefaults() {
        LevelData data = new LevelData();
        assertEquals("", data.getName());
        assertEquals(Difficulty.EASY, data.getDifficulty());
        assertEquals("", data.getObjective());
        assertTrue(data.getObjects().isEmpty());
        assertTrue(data.getLimits().isEmpty());
    }

    @Test
    void testConstructorWithoutLimits() {
        var obj = new TennisballConf(1,2,0,false);
        LevelData data = new LevelData("L", Difficulty.HARD, "O", List.of(obj));
        assertEquals("L", data.getName());
        assertEquals(Difficulty.HARD, data.getDifficulty());
        assertEquals("O", data.getObjective());
        assertEquals(List.of(obj), data.getObjects());
        assertTrue(data.getLimits().isEmpty());
    }
} 
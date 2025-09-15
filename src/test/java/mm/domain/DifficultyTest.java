package mm.domain;

import mm.domain.storage.Difficulty;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DifficultyTest {

    @Test
    void testFromStringValidValues() {
        assertEquals(Difficulty.EASY, Difficulty.fromString("easy"));
        assertEquals(Difficulty.MEDIUM, Difficulty.fromString("MEDIUM"));
        assertEquals(Difficulty.HARD, Difficulty.fromString("Hard"));
        assertEquals(Difficulty.CUSTOM, Difficulty.fromString("custom"));
    }

    @Test
    void testFromStringInvalidValueThrows() {
        assertThrows(IllegalArgumentException.class, () -> Difficulty.fromString("invalid"));
    }
} 
package mm.game.level;

public class LevelStorage {

    private static String lastLevelFile = "level1.json"; // Standardwert

    public static String getLastLevelFile() {
        return lastLevelFile;
    }

    public static void setLastLevelFile(String filename) {
        lastLevelFile = filename;
    }
}

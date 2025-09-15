package mm.core.storage;

public enum Difficulty {
    EASY, 
    MEDIUM, 
    HARD, 
    CUSTOM;

    public static Difficulty fromString(String difficulty) {
        return Difficulty.valueOf(difficulty.toUpperCase());
    }
}
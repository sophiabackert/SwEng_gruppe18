package mm.gui.menu;

public class Settings {
    private static double volume = 25.0; // Bereich: 0.0 bis 100.0
    private static boolean muted = false;

    public static double getVolume() {
        return volume;
    }

    public static void setVolume(double volume) {
        Settings.volume = volume;
    }

    public static boolean isMuted() {
        return muted;
    }

    public static void setMuted(boolean muted) {
        Settings.muted = muted;
    }
}

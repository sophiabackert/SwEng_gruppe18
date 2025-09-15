package mm.gui;

/**
 * Interface für Observer, die über Einstellungsänderungen benachrichtigt werden sollen.
 */
public interface SettingsObserver {
    /**
     * Wird aufgerufen, wenn sich die FPS-Einstellung ändert.
     * @param newFPS Der neue FPS-Wert
     */
    default void onFPSChanged(double newFPS) {}

    /**
     * Wird aufgerufen, wenn sich die Lautstärke ändert.
     * @param newVolume Die neue Lautstärke (0.0 - 1.0)
     */
    default void onVolumeChanged(double newVolume) {}

    /**
     * Wird aufgerufen, wenn sich die Gravitationseinstellungen ändern.
     * @param strength Die neue Gravitationsstärke
     * @param angle Der neue Gravitationswinkel
     */
    default void onGravityChanged(double strength, double angle) {}

    /**
     * Wird aufgerufen, wenn sich die Auflösung ändert.
     * @param width Die neue Breite
     * @param height Die neue Höhe
     * @param fullscreen Ob Vollbild aktiviert ist
     */
    default void onResolutionChanged(int width, int height, boolean fullscreen) {}

    /**
     * Wird aufgerufen, wenn sich die Barrierefreiheit-Einstellung ändert.
     * @param enabled Ob Barrierefreiheit aktiviert ist
     */
    default void onAccessibilityChanged(boolean enabled) {}
} 
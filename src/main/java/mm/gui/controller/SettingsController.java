package mm.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller für das Einstellungsmenü.
 * <p>
 * Ermöglicht die Anpassung der Ziel-FPS und das Aktivieren/Deaktivieren des Zeitlupenmodus.
 * Stellt statische Methoden zur Steuerung der Framerate und Zeitlupe bereit.
 * </p>
 */
public class SettingsController extends Controller implements Initializable {

    /**
     * Slider zur Einstellung der Ziel-FPS.
     */
    @FXML
    private Slider fpsSlider;
    
    /**
     * Label zur Anzeige der aktuellen FPS.
     */
    @FXML
    private Label fpsLabel;
    
    /**
     * Umschalter für den Zeitlupenmodus.
     */
    @FXML
    private ToggleButton slowMotionToggle;

    private static double targetFPS = 60.0;
    private static boolean slowMotionEnabled = false;
    private static long lastFrameTime = 0;
    private static final double SLOW_MOTION_SCALE = 0.25;

    /**
     * Initialisiert die Einstellungen (Slider, Toggle).
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupFPSSlider();
        setupSlowMotionToggle();
    }

    /**
     * Initialisiert den FPS-Slider und dessen Listener.
     */
    private void setupFPSSlider() {
        if (fpsSlider != null && fpsLabel != null) {
            fpsLabel.setText((int) targetFPS + " FPS");
            fpsSlider.setValue(targetFPS);
            
            fpsSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                targetFPS = newValue.doubleValue();
                fpsLabel.setText((int) targetFPS + " FPS");
            });
        }
    }

    /**
     * Initialisiert den Zeitlupen-Umschalter und dessen Listener.
     */
    private void setupSlowMotionToggle() {
        if (slowMotionToggle != null) {
            slowMotionToggle.setSelected(slowMotionEnabled);
            updateSlowMotionButtonText();
            
            slowMotionToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
                slowMotionEnabled = newValue;
                updateSlowMotionButtonText();
            });
        }
    }

    /**
     * Aktualisiert den Text des Zeitlupen-Umschalters.
     */
    private void updateSlowMotionButtonText() {
        if (slowMotionToggle != null) {
            slowMotionToggle.setText(slowMotionEnabled ? "Zeitlupe" : "Normal");
        }
    }

    /**
     * Handler für den Zurück-Button. Wechselt zurück ins Hauptmenü.
     */
    @FXML
    private void handleBack() {
        if (viewManager != null) {
            viewManager.showMainMenu();
        }
    }

    /**
     * Gibt an, ob ein Frame-Update erfolgen soll (basierend auf Ziel-FPS).
     * @return true, wenn ein Update erfolgen soll
     */
    public static boolean shouldUpdate() {
        long currentTime = System.nanoTime();
        double targetFrameTime = 1.0 / targetFPS * 1_000_000_000.0;
        
        if (lastFrameTime == 0) {
            lastFrameTime = currentTime;
            return true;
        }
        
        long timeSinceLastFrame = currentTime - lastFrameTime;
        
        if (timeSinceLastFrame >= (targetFrameTime * 0.9)) {
            lastFrameTime = currentTime;
            return true;
        }
        
        return false;
    }

    /**
     * Gibt den aktuellen Zeitfaktor zurück (1.0 oder Zeitlupenfaktor).
     * @return Zeitfaktor
     */
    public static double getTimeScale() {
        return slowMotionEnabled ? SLOW_MOTION_SCALE : 1.0;
    }

    /**
     * Gibt die aktuelle Ziel-FPS zurück.
     * @return Ziel-FPS
     */
    public static double getTargetFPS() {
        return targetFPS;
    }

    /**
     * Setzt die Ziel-FPS (zwischen 30 und 120).
     * @param fps Ziel-FPS
     */
    public static void setTargetFPS(double fps) {
        targetFPS = Math.max(30, Math.min(120, fps));
    }

    /**
     * Gibt zurück, ob der Zeitlupenmodus aktiviert ist.
     * @return true, wenn Zeitlupe aktiv ist
     */
    public static boolean isSlowMotionEnabled() {
        return slowMotionEnabled;
    }

    /**
     * Aktiviert oder deaktiviert den Zeitlupenmodus.
     * @param enabled true für Zeitlupe, false für normal
     */
    public static void setSlowMotionEnabled(boolean enabled) {
        slowMotionEnabled = enabled;
    }

    /**
     * Setzt den Frame-Timer zurück (z.B. nach FPS-Änderung).
     */
    public static void resetFPSTimer() {
        lastFrameTime = 0;
    }
} 
package mm.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller für die Einstellungen mit FPS und Slow Motion.
 */
public class SettingsController extends Controller implements Initializable {

    @FXML
    private Slider fpsSlider;
    
    @FXML
    private Label fpsLabel;
    
    @FXML
    private ToggleButton slowMotionToggle;

    // Statische Variablen für die Spielsteuerung
    private static double targetFPS = 60.0;
    private static boolean slowMotionEnabled = false;
    private static long lastFrameTime = 0;
    private static final double SLOW_MOTION_SCALE = 0.25;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupFPSSlider();
        setupSlowMotionToggle();
    }

    private void setupFPSSlider() {
        if (fpsSlider != null && fpsLabel != null) {
            // Zeige aktuellen FPS-Wert an
            fpsLabel.setText((int) targetFPS + " FPS");
            fpsSlider.setValue(targetFPS);
            
            // Listener für FPS-Änderungen
            fpsSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                targetFPS = newValue.doubleValue();
                fpsLabel.setText((int) targetFPS + " FPS");
            });
        }
    }

    private void setupSlowMotionToggle() {
        if (slowMotionToggle != null) {
            // Setze initial state
            slowMotionToggle.setSelected(slowMotionEnabled);
            updateSlowMotionButtonText();
            
            // Listener für Slow Motion Toggle
            slowMotionToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
                slowMotionEnabled = newValue;
                updateSlowMotionButtonText();
            });
        }
    }

    private void updateSlowMotionButtonText() {
        if (slowMotionToggle != null) {
            slowMotionToggle.setText(slowMotionEnabled ? "Zeitlupe" : "Normal");
        }
    }

    @FXML
    private void handleBack() {
        if (viewManager != null) {
            viewManager.showMainMenu();
        }
    }

    // Statische Methoden für die Spielsteuerung
    
    /**
     * Überprüft, ob ein neuer Frame gerendert werden soll basierend auf der FPS-Einstellung.
     * @return true wenn ein Update durchgeführt werden soll
     */
    public static boolean shouldUpdate() {
        long currentTime = System.nanoTime();
        double targetFrameTime = 1.0 / targetFPS * 1_000_000_000.0; // Umrechnung in Nanosekunden
        
        if (lastFrameTime == 0) {
            lastFrameTime = currentTime;
            return true;
        }
        
        long timeSinceLastFrame = currentTime - lastFrameTime;
        
        // Weniger restriktive FPS-Limitierung - erlaube kleine Abweichungen
        if (timeSinceLastFrame >= (targetFrameTime * 0.9)) {
            lastFrameTime = currentTime;
            return true;
        }
        
        return false;
    }

    /**
     * Gibt den aktuellen Zeitmaßstab zurück.
     * @return 1.0 für normale Geschwindigkeit, 0.25 für Slow Motion
     */
    public static double getTimeScale() {
        return slowMotionEnabled ? SLOW_MOTION_SCALE : 1.0;
    }

    /**
     * Gibt die aktuelle Ziel-FPS zurück.
     * @return Die Ziel-FPS
     */
    public static double getTargetFPS() {
        return targetFPS;
    }

    /**
     * Setzt die Ziel-FPS.
     * @param fps Die neue Ziel-FPS
     */
    public static void setTargetFPS(double fps) {
        targetFPS = Math.max(30, Math.min(120, fps));
    }

    /**
     * Gibt zurück, ob Slow Motion aktiviert ist.
     * @return true wenn Slow Motion aktiviert ist
     */
    public static boolean isSlowMotionEnabled() {
        return slowMotionEnabled;
    }

    /**
     * Schaltet Slow Motion ein oder aus.
     * @param enabled true um Slow Motion zu aktivieren
     */
    public static void setSlowMotionEnabled(boolean enabled) {
        slowMotionEnabled = enabled;
    }

    /**
     * Setzt den FPS-Timer zurück (nützlich beim Spielstart).
     */
    public static void resetFPSTimer() {
        lastFrameTime = 0;
    }
} 
package mm.gui;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.stage.Screen;

public class GuiSettingsController extends GuiController {
    @FXML
    private Slider volumeSlider;
    
    @FXML
    private Slider fpsSlider;
    
    @FXML
    private Label fpsLabel;
    
    @FXML
    private ComboBox<String> resolutionComboBox;
    
    @FXML
    private CheckBox fullscreenCheckBox;
    
    @FXML
    private CheckBox accessibilityCheckBox;

    @FXML
    private Slider gravityStrengthSlider;

    @FXML
    private Label gravityStrengthLabel;

    @FXML
    private Slider gravityAngleSlider;

    @FXML
    private Label gravityAngleLabel;

    private GravityManager gravityManager;
    private AudioManager audioManager;
    private FPSManager fpsManager;
    private ResolutionManager resManager;
    private AccessibilityManager accessManager;

    @FXML
    public void initialize() {
        // Initialisiere die Manager
        gravityManager = GravityManager.getInstance();
        audioManager = AudioManager.getInstance();
        fpsManager = FPSManager.getInstance();
        resManager = ResolutionManager.getInstance();
        accessManager = AccessibilityManager.getInstance();

        // Fülle die Auflösungsoptionen
        Screen primaryScreen = Screen.getPrimary();
        double width = primaryScreen.getBounds().getWidth();
        double height = primaryScreen.getBounds().getHeight();
        
        resolutionComboBox.getItems().addAll(
            "1280x720",
            "1920x1080",
            width + "x" + height
        );
        
        // FPS-Slider Listener
        fpsSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int fps = newVal.intValue();
            fpsLabel.setText(fps + " FPS");
            fpsManager.setTargetFPS(fps);
        });

        // Lautstärke-Slider Listener
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double volume = newVal.doubleValue() / 100.0; // Konvertiere von Prozent zu Dezimal
            audioManager.setMasterVolume(volume);
        });

        // Gravitations-Slider Listener
        gravityStrengthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double strength = newVal.doubleValue();
            gravityStrengthLabel.setText(String.format("%.2f m/s²", strength));
            gravityManager.setGravityStrength(strength);
        });

        gravityAngleSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int angle = newVal.intValue();
            gravityAngleLabel.setText(angle + "°");
            gravityManager.setGravityAngle(angle);
        });

        // Auflösungs-Listener
        resolutionComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String[] dimensions = newVal.split("x");
                int newWidth = Integer.parseInt(dimensions[0]);
                int newHeight = Integer.parseInt(dimensions[1]);
                resManager.setResolution(newWidth, newHeight);
            }
        });

        // Vollbild-Listener
        fullscreenCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            resManager.setFullscreen(newVal);
        });

        // Barrierefreiheit-Listener
        accessibilityCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            accessManager.setEnabled(newVal);
        });
        
        // Lade aktuelle Einstellungen
        loadCurrentSettings();
    }

    private void loadCurrentSettings() {
        // Lade die Lautstärke
        volumeSlider.setValue(audioManager.getMasterVolume() * 100); // Konvertiere von Dezimal zu Prozent

        // Lade die aktuelle FPS-Einstellung
        fpsSlider.setValue(fpsManager.getTargetFPS());

        // Lade die aktuelle Auflösung
        String currentResolution = resManager.getCurrentWidth() + "x" + resManager.getCurrentHeight();
        resolutionComboBox.setValue(currentResolution);

        // Lade Vollbild-Einstellung
        fullscreenCheckBox.setSelected(resManager.isFullscreen());

        // Lade Barrierefreiheit-Einstellung
        accessibilityCheckBox.setSelected(accessManager.isEnabled());

        // Lade Gravitationseinstellungen
        gravityStrengthSlider.setValue(gravityManager.getGravityStrength());
        gravityAngleSlider.setValue(gravityManager.getGravityAngle());
    }

    @FXML
    private void handleBack() {
        try {
            getViewManager().showMainMenu();
        } catch (NavigationException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSave() {
        try {
            getViewManager().showMainMenu();
        } catch (NavigationException e) {
            e.printStackTrace();
        }
    }
} 
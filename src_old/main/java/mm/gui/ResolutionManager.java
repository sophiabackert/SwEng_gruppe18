package mm.gui;

import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Manager-Klasse für die Behandlung verschiedener Bildschirmauflösungen und Fenstermodi.
 */
public class ResolutionManager {
    private static final Logger LOGGER = Logger.getLogger(ResolutionManager.class.getName());
    private static ResolutionManager instance;
    
    // Standard-Auflösung (16:9)
    public static final double DEFAULT_WIDTH = 1280;
    public static final double DEFAULT_HEIGHT = 720;
    private static final double MIN_WIDTH = 800;
    private static final double MIN_HEIGHT = 600;
    
    private final Preferences preferences;
    private Stage primaryStage;
    private int currentWidth = 1280;
    private int currentHeight = 720;
    private boolean fullscreen = false;
    private double lastWindowWidth;
    private double lastWindowHeight;
    private double lastWindowX;
    private double lastWindowY;
    
    private ResolutionManager() {
        preferences = Preferences.userNodeForPackage(ResolutionManager.class);
        loadSettings();
    }
    
    /**
     * Gibt die einzige Instanz des ResolutionManagers zurück.
     * @return Die ResolutionManager-Instanz
     */
    public static ResolutionManager getInstance() {
        if (instance == null) {
            instance = new ResolutionManager();
        }
        return instance;
    }
    
    /**
     * Initialisiert den ResolutionManager mit der primären Stage.
     * @param stage Die primäre JavaFX Stage
     */
    public void initialize(Stage stage) {
        this.primaryStage = stage;
        
        // Minimale Fenstergröße setzen
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        
        // Fensterposition und -größe wiederherstellen
        if (!fullscreen) {
            stage.setWidth(lastWindowWidth);
            stage.setHeight(lastWindowHeight);
            stage.setX(lastWindowX);
            stage.setY(lastWindowY);
        }
        
        // Vollbildmodus setzen
        stage.setFullScreen(fullscreen);
        
        // Event-Handler für Fensteränderungen
        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (!stage.isFullScreen()) {
                lastWindowWidth = newVal.doubleValue();
                saveSettings();
            }
        });
        
        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (!stage.isFullScreen()) {
                lastWindowHeight = newVal.doubleValue();
                saveSettings();
            }
        });
        
        stage.xProperty().addListener((obs, oldVal, newVal) -> {
            if (!stage.isFullScreen()) {
                lastWindowX = newVal.doubleValue();
                saveSettings();
            }
        });
        
        stage.yProperty().addListener((obs, oldVal, newVal) -> {
            if (!stage.isFullScreen()) {
                lastWindowY = newVal.doubleValue();
                saveSettings();
            }
        });
        
        stage.fullScreenProperty().addListener((obs, oldVal, newVal) -> {
            fullscreen = newVal;
            saveSettings();
        });
    }
    
    /**
     * Lädt die gespeicherten Einstellungen.
     */
    private void loadSettings() {
        fullscreen = preferences.getBoolean("fullscreen", false);
        lastWindowWidth = preferences.getDouble("windowWidth", DEFAULT_WIDTH);
        lastWindowHeight = preferences.getDouble("windowHeight", DEFAULT_HEIGHT);
        lastWindowX = preferences.getDouble("windowX", -1);
        lastWindowY = preferences.getDouble("windowY", -1);
        
        // Wenn keine Position gespeichert wurde, zentriere das Fenster
        if (lastWindowX == -1 || lastWindowY == -1) {
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            lastWindowX = (screenBounds.getWidth() - lastWindowWidth) / 2;
            lastWindowY = (screenBounds.getHeight() - lastWindowHeight) / 2;
        }
    }
    
    /**
     * Speichert die aktuellen Einstellungen.
     */
    private void saveSettings() {
        preferences.putBoolean("fullscreen", fullscreen);
        preferences.putDouble("windowWidth", lastWindowWidth);
        preferences.putDouble("windowHeight", lastWindowHeight);
        preferences.putDouble("windowX", lastWindowX);
        preferences.putDouble("windowY", lastWindowY);
    }
    
    /**
     * Aktualisiert die Skalierung einer Szene basierend auf der aktuellen Fenstergröße.
     * @param scene Die zu skalierende Szene
     */
    public void updateScaling(Scene scene) {
        if (scene == null || scene.getWidth() < 2 || scene.getHeight() < 2) {
            return;     // 0×0 oder 1×1 ignorieren
        }

        if (primaryStage == null) {
            LOGGER.warning("ResolutionManager wurde nicht initialisiert");
            return;
        }

        double scaleX = primaryStage.getWidth() / DEFAULT_WIDTH;
        double scaleY = primaryStage.getHeight() / DEFAULT_HEIGHT;
        double scale = Math.min(scaleX, scaleY);

        scene.getRoot().setScaleX(scale);
        scene.getRoot().setScaleY(scale);

        // Zentriere den Inhalt
        double translateX = (primaryStage.getWidth() - (DEFAULT_WIDTH * scale)) / 2;
        double translateY = (primaryStage.getHeight() - (DEFAULT_HEIGHT * scale)) / 2;
        scene.getRoot().setTranslateX(translateX);
        scene.getRoot().setTranslateY(translateY);
    }
    
    /**
     * Wechselt zwischen Vollbild- und Fenstermodus.
     */
    public void toggleFullscreen() {
        if (primaryStage != null) {
            primaryStage.setFullScreen(!primaryStage.isFullScreen());
        }
    }
    
    /**
     * Gibt zurück, ob sich die Anwendung im Vollbildmodus befindet.
     * @return true wenn im Vollbildmodus, sonst false
     */
    public boolean isFullscreen() {
        return fullscreen;
    }
    
    /**
     * Setzt die Anwendung in den Vollbildmodus.
     * @param fullscreen true für Vollbildmodus, false für Fenstermodus
     */
    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
        if (primaryStage != null) {
            primaryStage.setFullScreen(fullscreen);
        }
    }
    
    /**
     * Initialisiert das Hauptfenster mit gespeicherten oder Standardeinstellungen.
     */
    public void initializeStage(Stage stage) {
        this.primaryStage = stage;
        
        // Minimale Fenstergröße setzen
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        
        // Gespeicherte Fenstergröße laden oder Standardgröße verwenden
        double width = preferences.getDouble("window.width", DEFAULT_WIDTH);
        double height = preferences.getDouble("window.height", DEFAULT_HEIGHT);
        stage.setWidth(width);
        stage.setHeight(height);
        
        // Gespeicherte Fensterposition laden
        double x = preferences.getDouble("window.x", -1);
        double y = preferences.getDouble("window.y", -1);
        if (x >= 0 && y >= 0) {
            stage.setX(x);
            stage.setY(y);
        } else {
            // Fenster zentrieren
            Screen primaryScreen = Screen.getPrimary();
            stage.setX((primaryScreen.getBounds().getWidth() - width) / 2);
            stage.setY((primaryScreen.getBounds().getHeight() - height) / 2);
        }
        
        // Gespeicherten Vollbildmodus laden
        boolean fullscreen = preferences.getBoolean("window.fullscreen", false);
        stage.setFullScreen(fullscreen);
        
        // Event-Handler für das Speichern der Fensterposition und -größe
        stage.xProperty().addListener((obs, old, newX) -> {
            if (!stage.isFullScreen()) {
                preferences.putDouble("window.x", newX.doubleValue());
            }
        });
        
        stage.yProperty().addListener((obs, old, newY) -> {
            if (!stage.isFullScreen()) {
                preferences.putDouble("window.y", newY.doubleValue());
            }
        });
        
        stage.widthProperty().addListener((obs, old, newWidth) -> {
            if (!stage.isFullScreen()) {
                preferences.putDouble("window.width", newWidth.doubleValue());
            }
        });
        
        stage.heightProperty().addListener((obs, old, newHeight) -> {
            if (!stage.isFullScreen()) {
                preferences.putDouble("window.height", newHeight.doubleValue());
            }
        });
        
        stage.fullScreenProperty().addListener((obs, old, newFullscreen) -> {
            preferences.putBoolean("window.fullscreen", newFullscreen);
        });
    }
    
    /**
     * Setzt die Auflösung des Fensters.
     */
    public void setResolution(double width, double height) {
        if (primaryStage != null && !primaryStage.isFullScreen()) {
            primaryStage.setWidth(Math.max(width, MIN_WIDTH));
            primaryStage.setHeight(Math.max(height, MIN_HEIGHT));
        }
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public int getCurrentWidth() {
        return currentWidth;
    }

    public int getCurrentHeight() {
        return currentHeight;
    }

    public void setResolution(int width, int height) {
        this.currentWidth = width;
        this.currentHeight = height;
        if (primaryStage != null) {
            primaryStage.setWidth(width);
            primaryStage.setHeight(height);
        }
    }
} 
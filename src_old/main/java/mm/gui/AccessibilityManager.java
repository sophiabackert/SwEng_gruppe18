package mm.gui;

import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Labeled;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.logging.Logger;

/**
 * Manager-Klasse für Barrierefreiheitsfunktionen.
 */
public class AccessibilityManager {
    private static final Logger LOGGER = Logger.getLogger(AccessibilityManager.class.getName());
    private static AccessibilityManager instance;
    
    private final Preferences preferences;
    private boolean highContrastMode;
    private boolean screenReaderMode;
    private double textScaleFactor;
    
    // Tastenkombinationen für Barrierefreiheitsfunktionen
    private static final KeyCombination HIGH_CONTRAST_KEY = new KeyCodeCombination(KeyCode.H, KeyCombination.ALT_DOWN, KeyCombination.CONTROL_DOWN);
    private static final KeyCombination SCREEN_READER_KEY = new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN, KeyCombination.CONTROL_DOWN);
    private static final KeyCombination INCREASE_TEXT_KEY = new KeyCodeCombination(KeyCode.PLUS, KeyCombination.CONTROL_DOWN);
    private static final KeyCombination DECREASE_TEXT_KEY = new KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN);
    
    private AccessibilityManager() {
        preferences = Preferences.userNodeForPackage(AccessibilityManager.class);
        loadSettings();
    }
    
    /**
     * Gibt die einzige Instanz des AccessibilityManagers zurück.
     * @return Die AccessibilityManager-Instanz
     */
    public static AccessibilityManager getInstance() {
        if (instance == null) {
            instance = new AccessibilityManager();
        }
        return instance;
    }
    
    /**
     * Lädt die gespeicherten Einstellungen.
     */
    private void loadSettings() {
        highContrastMode = preferences.getBoolean("highContrastMode", false);
        screenReaderMode = preferences.getBoolean("screenReaderMode", false);
        textScaleFactor = preferences.getDouble("textScaleFactor", 1.0);
    }
    
    /**
     * Speichert die aktuellen Einstellungen.
     */
    private void saveSettings() {
        preferences.putBoolean("highContrastMode", highContrastMode);
        preferences.putBoolean("screenReaderMode", screenReaderMode);
        preferences.putDouble("textScaleFactor", textScaleFactor);
    }
    
    /**
     * Initialisiert die Barrierefreiheitsfunktionen für eine Szene.
     * @param scene Die zu initialisierende Szene
     */
    public void initializeScene(Scene scene) {
        // Tastaturkürzel einrichten
        scene.setOnKeyPressed(event -> {
            if (HIGH_CONTRAST_KEY.match(event)) {
                toggleHighContrastMode(scene);
            } else if (SCREEN_READER_KEY.match(event)) {
                toggleScreenReaderMode(scene);
            } else if (INCREASE_TEXT_KEY.match(event)) {
                adjustTextSize(scene, 1.1);
            } else if (DECREASE_TEXT_KEY.match(event)) {
                adjustTextSize(scene, 0.9);
            }
        });
        
        // Aktuelle Einstellungen anwenden
        applyHighContrastMode(scene, highContrastMode);
        applyScreenReaderMode(scene, screenReaderMode);
        applyTextScaling(scene, textScaleFactor);
        
        // Tab-Navigation aktivieren
        enableTabNavigation(scene);
    }
    
    /**
     * Aktiviert oder deaktiviert den Hochkontrastmodus.
     * @param scene Die aktuelle Szene
     */
    public void toggleHighContrastMode(Scene scene) {
        highContrastMode = !highContrastMode;
        applyHighContrastMode(scene, highContrastMode);
        saveSettings();
    }
    
    /**
     * Wendet den Hochkontrastmodus auf eine Szene an.
     * @param scene Die Szene
     * @param enabled Ob der Modus aktiviert werden soll
     */
    private void applyHighContrastMode(Scene scene, boolean enabled) {
        if (enabled) {
            scene.getRoot().getStyleClass().add("high-contrast");
        } else {
            scene.getRoot().getStyleClass().remove("high-contrast");
        }
    }
    
    /**
     * Aktiviert oder deaktiviert den Screenreader-Modus.
     * @param scene Die aktuelle Szene
     */
    public void toggleScreenReaderMode(Scene scene) {
        screenReaderMode = !screenReaderMode;
        applyScreenReaderMode(scene, screenReaderMode);
        saveSettings();
    }
    
    /**
     * Wendet den Screenreader-Modus auf eine Szene an.
     * @param scene Die Szene
     * @param enabled Ob der Modus aktiviert werden soll
     */
    private void applyScreenReaderMode(Scene scene, boolean enabled) {
        List<Node> nodes = getAllNodes(scene.getRoot());
        for (Node node : nodes) {
            if (node instanceof Control) {
                Control control = (Control) node;
                if (enabled) {
                    // Verbesserte Beschreibungen für Screenreader
                    if (control instanceof Labeled) {
                        Labeled labeled = (Labeled) control;
                        String text = labeled.getText();
                        if (text != null && !text.isEmpty()) {
                            control.setAccessibleText(text);
                            control.setAccessibleHelp("Klicken Sie hier, um " + text.toLowerCase() + " auszuführen");
                        }
                    }
                } else {
                    control.setAccessibleText(null);
                    control.setAccessibleHelp(null);
                }
            }
        }
    }
    
    /**
     * Passt die Textgröße an.
     * @param scene Die aktuelle Szene
     * @param factor Der Skalierungsfaktor
     */
    public void adjustTextSize(Scene scene, double factor) {
        textScaleFactor *= factor;
        applyTextScaling(scene, textScaleFactor);
        saveSettings();
    }
    
    /**
     * Wendet die Textskalierung auf eine Szene an.
     * @param scene Die Szene
     * @param scale Der Skalierungsfaktor
     */
    private void applyTextScaling(Scene scene, double scale) {
        scene.getRoot().setStyle("-fx-font-size: " + (12 * scale) + "px;");
    }
    
    /**
     * Aktiviert die Tab-Navigation für eine Szene.
     * @param scene Die Szene
     */
    private void enableTabNavigation(Scene scene) {
        List<Node> nodes = getAllNodes(scene.getRoot());
        for (Node node : nodes) {
            if (node instanceof Control) {
                Control control = (Control) node;
                control.setFocusTraversable(true);
            }
        }
    }
    
    /**
     * Sammelt alle Nodes in einer Parent-Node.
     * @param parent Die Parent-Node
     * @return Liste aller Nodes
     */
    private List<Node> getAllNodes(Parent parent) {
        List<Node> nodes = new ArrayList<>();
        addAllDescendants(parent, nodes);
        return nodes;
    }
    
    /**
     * Fügt alle Nachfahren einer Node zur Liste hinzu.
     * @param parent Die Parent-Node
     * @param nodes Die Liste der Nodes
     */
    private void addAllDescendants(Parent parent, List<Node> nodes) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            nodes.add(node);
            if (node instanceof Parent) {
                addAllDescendants((Parent) node, nodes);
            }
        }
    }
    
    /**
     * Gibt zurück, ob der Hochkontrastmodus aktiviert ist.
     * @return true wenn aktiviert, sonst false
     */
    public boolean isHighContrastMode() {
        return highContrastMode;
    }
    
    /**
     * Gibt zurück, ob der Screenreader-Modus aktiviert ist.
     * @return true wenn aktiviert, sonst false
     */
    public boolean isScreenReaderMode() {
        return screenReaderMode;
    }
    
    /**
     * Gibt den aktuellen Textskalierungsfaktor zurück.
     * @return Der Skalierungsfaktor
     */
    public double getTextScaleFactor() {
        return textScaleFactor;
    }

    private boolean enabled = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        // TODO: Implementiere die Anwendung von Barrierefreiheits-Einstellungen
    }
} 
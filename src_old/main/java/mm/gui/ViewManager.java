package mm.gui;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.beans.value.ChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton-Klasse zur Verwaltung der verschiedenen Ansichten der Anwendung.
 * Implementiert fortgeschrittene Navigationsfunktionen wie Übergangseffekte und Szenen-Caching.
 */
public class ViewManager {
    private static final Logger LOGGER = Logger.getLogger(ViewManager.class.getName());
    private static ViewManager instance;
    private static final double TRANSITION_DURATION = 0.3; // Sekunden
    
    /**
     * Verfügbare Ansichten in der Anwendung.
     */
    public enum View {
        MAIN_MENU("/fxml/main_menu.fxml"),
        LEVEL_SELECTION("/fxml/level_selection.fxml"),
        LEVEL_EDITOR("/fxml/level_editor.fxml"),
        SETTINGS("/fxml/settings_menu.fxml"),
        GAME("/fxml/game.fxml"),
        GAME_EDITOR("/fxml/game_editor.fxml");

        private final String fxmlPath;

        View(String fxmlPath) {
            this.fxmlPath = fxmlPath;
        }

        public String getFxmlPath() {
            return fxmlPath;
        }
    }
    
    // Cache für geladene Szenen
    private final Map<View, Pair<Scene, Object>> sceneCache = new HashMap<>();
    
    // Navigationsdaten für den Szenenwechsel
    private final Map<String, Object> navigationData = new HashMap<>();
    
    private Stage primaryStage;
    private View currentView;
    private Scene currentScene;
    
    private ChangeListener<Number> currentResizeListener;
    
    private ViewManager() {
        // Private Konstruktor für Singleton
    }
    
    /**
     * Gibt die einzige Instanz des ViewManagers zurück.
     * @return Die ViewManager-Instanz
     */
    public static ViewManager getInstance() {
        if (instance == null) {
            instance = new ViewManager();
        }
        return instance;
    }
    
    /**
     * Initialisiert den ViewManager mit der primären Stage.
     * @param stage Die primäre JavaFX Stage
     */
    public void initialize(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Mad Machines");
        
        // Stage-Größe VOR dem Laden der ersten Szene setzen
        stage.setWidth(ResolutionManager.DEFAULT_WIDTH);
        stage.setHeight(ResolutionManager.DEFAULT_HEIGHT);
        
        ResolutionManager.getInstance().initializeStage(stage);
    }
    
    /**
     * Navigiert zu einer bestimmten Ansicht mit Übergangseffekt.
     * @param view Die Zielansicht
     * @throws NavigationException wenn die Navigation fehlschlägt
     */
    public void navigateTo(View view) throws NavigationException {
        if (view == null) {
            throw new NavigationException("View darf nicht null sein");
        }

        Pair<Scene, Object> loadResult = loadScene(view);
        Scene scene = loadResult.getKey();

        if (primaryStage.getScene() == null) {
            // Handler VOR show() registrieren
            primaryStage.setOnShown(ev -> {
                addResizeListeners(scene);
                ResolutionManager.getInstance().updateScaling(scene);
            });

            // Szene setzen und Fenster öffnen
            primaryStage.setScene(scene);
            currentView = view;
            primaryStage.show();
            return;
        }

        // Normale Navigation mit Fade-Effekt
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), primaryStage.getScene().getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(e -> {
            primaryStage.setScene(scene);
            currentView = view;
            
            // Listener für neue Szene registrieren
            addResizeListeners(scene);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), scene.getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }
    
    /**
     * Lädt eine FXML-Datei und erstellt eine neue Szene.
     * @param view Die zu ladende Ansicht
     * @return Ein Pair aus der erstellten Szene und dem Controller
     */
    private Pair<Scene, Object> loadScene(View view) throws NavigationException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(view.getFxmlPath()));
            Parent root = loader.load();
            
            // Szene mit expliziter Größe erstellen
            Scene scene = new Scene(root, ResolutionManager.DEFAULT_WIDTH, ResolutionManager.DEFAULT_HEIGHT);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            AccessibilityManager.getInstance().initializeScene(scene);
            
            // ViewManager im Controller setzen
            Object controller = loader.getController();
            if (controller instanceof GuiController) {
                ((GuiController) controller).setViewManager(this);
            }
            
            return new Pair<>(scene, controller);
        } catch (IOException e) {
            throw new NavigationException("Fehler beim Laden von " + view.getFxmlPath(), e);
        }
    }
    
    /**
     * Setzt Navigationsdaten für den nächsten Szenenwechsel.
     * @param key Der Schlüssel für die Daten
     * @param value Der Wert der Daten
     */
    public void setNavigationData(String key, Object value) {
        navigationData.put(key, value);
    }
    
    /**
     * Ruft Navigationsdaten ab.
     * @param key Der Schlüssel der Daten
     * @return Der gespeicherte Wert oder null
     */
    public Object getNavigationData(String key) {
        return navigationData.get(key);
    }
    
    /**
     * Löscht alle Navigationsdaten.
     */
    public void clearNavigationData() {
        navigationData.clear();
    }
    
    /**
     * Leert den Szenen-Cache.
     */
    public void clearCache() {
        sceneCache.clear();
    }
    
    /**
     * Gibt die aktuelle Ansicht zurück.
     * @return Die aktuelle Ansicht
     */
    public View getCurrentView() {
        return currentView;
    }
    
    /**
     * Wechselt zwischen Vollbild- und Fenstermodus.
     */
    public void toggleFullscreen() {
        ResolutionManager.getInstance().toggleFullscreen();
    }
    
    /**
     * Setzt den Vollbildmodus.
     * @param fullscreen true für Vollbildmodus, false für Fenstermodus
     */
    public void setFullscreen(boolean fullscreen) {
        ResolutionManager.getInstance().setFullscreen(fullscreen);
    }

    /**
     * Zeigt das Hauptmenü an.
     * @throws NavigationException wenn das Hauptmenü nicht geladen werden kann
     */
    public void showMainMenu() throws NavigationException {
        navigateTo(View.MAIN_MENU);
    }

    /**
     * Zeigt die Level-Auswahl an.
     * @throws NavigationException wenn die Level-Auswahl nicht geladen werden kann
     */
    public void showLevelSelection() throws NavigationException {
        navigateTo(View.LEVEL_SELECTION);
    }

    /**
     * Zeigt den Level-Editor an.
     * @throws NavigationException wenn der Level-Editor nicht geladen werden kann
     */
    public void showLevelEditor() throws NavigationException {
        navigateTo(View.LEVEL_EDITOR);
    }

    /**
     * Zeigt die Einstellungen an.
     * @throws NavigationException wenn die Einstellungen nicht geladen werden können
     */
    public void showSettings() throws NavigationException {
        navigateTo(View.SETTINGS);
    }

    /**
     * Zeigt das Spiel an.
     * @throws NavigationException wenn das Spiel nicht geladen werden kann
     */
    public GuiController showGame() throws NavigationException {
        Pair<Scene, Object> loadResult = loadScene(View.GAME);
        Scene scene = loadResult.getKey();
        Object controller = loadResult.getValue();
        
        // Szene setzen
        primaryStage.setScene(scene);
        currentView = View.GAME;
        
        // Listener für neue Szene registrieren
        addResizeListeners(scene);
        
        return (GuiController) controller;
    }

    /**
     * Zeigt den Game Editor an.
     * @throws NavigationException wenn der Game Editor nicht geladen werden kann
     */
    public GuiController showGameEditor() throws NavigationException {
        Pair<Scene, Object> loadResult = loadScene(View.GAME_EDITOR);
        Scene scene = loadResult.getKey();
        Object controller = loadResult.getValue();
        
        // Szene setzen
        primaryStage.setScene(scene);
        currentView = View.GAME_EDITOR;
        
        // Listener für neue Szene registrieren
        addResizeListeners(scene);
        
        return (GuiController) controller;
    }

    public void switchToMainMenu() throws NavigationException {
        navigateTo(View.MAIN_MENU);
    }

    public void switchToSettings() throws NavigationException {
        navigateTo(View.SETTINGS);
    }

    public Scene getCurrentScene() {
        return currentScene;
    }

    private void addResizeListeners(Scene scene) {
        if (scene.getUserData() != null) return;   // schon angebracht
        primaryStage.widthProperty().addListener((obs,o,n) ->
            ResolutionManager.getInstance().updateScaling(scene));
        primaryStage.heightProperty().addListener((obs,o,n) ->
            ResolutionManager.getInstance().updateScaling(scene));
        scene.setUserData(Boolean.TRUE);           // Marker
    }
}
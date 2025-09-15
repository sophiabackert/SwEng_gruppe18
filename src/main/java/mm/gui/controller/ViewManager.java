package mm.gui.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Zentrale Klasse zur Verwaltung und zum Wechseln der GUI-Views (Szenen).
 * <p>
 * Lädt FXML-Dateien, verwaltet den Haupt-Stage und sorgt für die Navigation zwischen den Ansichten.
 * </p>
 */
public class ViewManager {
    private static ViewManager instance;
    private Stage primaryStage;
    
    /**
     * Privater Konstruktor (Singleton).
     */
    private ViewManager() {
    }
    
    /**
     * Gibt die Singleton-Instanz des ViewManagers zurück.
     * @return Instanz des ViewManagers
     */
    public static ViewManager getInstance() {
        if (instance == null) {
            instance = new ViewManager();
        }
        return instance;
    }
    
    /**
     * Initialisiert den ViewManager mit dem Haupt-Stage.
     * @param stage Primärer JavaFX-Stage
     */
    public void initialize(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Mad Machines");
        stage.setWidth(1920);
        stage.setHeight(1080);
    }
    
    /**
     * Zeigt das Hauptmenü an.
     */
    public void showMainMenu() {
        loadScene("/fxml/main_menu.fxml");
    }
    
    /**
     * Zeigt die Levelauswahl an.
     */
    public void showLevelSelection() {
        loadScene("/fxml/level_selection.fxml");
    }
    
    /**
     * Zeigt den Level-Editor an.
     */
    public void showLevelEditor() {
        loadScene("/fxml/level_editor.fxml");
    }
    
    /**
     * Zeigt den Game-Editor an.
     */
    public void showGameEditor() {
        loadScene("/fxml/game_editor.fxml");
    }
    
    /**
     * Zeigt den Game-Editor mit einem bestimmten Level an.
     * @param levelFileName Name der Leveldatei
     */
    public void showGameEditorWithLevel(String levelFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game_editor.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1920, 1080);
            
            try {
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            } catch (Exception e) {
            }
            
            Object controller = loader.getController();
            if (controller instanceof GameEditorController) {
                GameEditorController gameController = (GameEditorController) controller;
                gameController.setViewManager(this);
                gameController.loadLevel(levelFileName);
            }
            
            primaryStage.setScene(scene);
            if (!primaryStage.isShowing()) {
                primaryStage.show();
            }
        } catch (Exception e) {
        }
    }
    
    /**
     * Zeigt das eigentliche Spiel an.
     */
    public void showGame() {
        loadScene("/fxml/game.fxml");
    }
    
    private Controller lastController;
    
    /**
     * Gibt den zuletzt geladenen Controller zurück.
     * @return Letzter Controller
     */
    public Controller getLastController() {
        return lastController;
    }
    
    /**
     * Zeigt das Einstellungsmenü an.
     */
    public void showSettings() {
        loadScene("/fxml/settings_menu.fxml");
    }
    
    /**
     * Wechselt zum Hauptmenü (Alias für showMainMenu).
     */
    public void switchToMainMenu() {
        showMainMenu();
    }
    
    /**
     * Lädt und zeigt eine Szene anhand des FXML-Pfads.
     * @param fxmlPath Pfad zur FXML-Datei
     */
    private void loadScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1920, 1080);
            
            try {
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            } catch (Exception e) {
            }
            
            Object controller = loader.getController();
            if (controller instanceof Controller) {
                ((Controller) controller).setViewManager(this);
                lastController = (Controller) controller;
            }
            
            primaryStage.setScene(scene);
            if (!primaryStage.isShowing()) {
                primaryStage.show();
            }
        } catch (Exception e) {
        }
    }
}
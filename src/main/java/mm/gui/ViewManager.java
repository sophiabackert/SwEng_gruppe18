package mm.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Einfacher ViewManager für die GUI-Navigation.
 */
public class ViewManager {
    private static ViewManager instance;
    private Stage primaryStage;
    
    private ViewManager() {
        // Singleton
    }
    
    public static ViewManager getInstance() {
        if (instance == null) {
            instance = new ViewManager();
        }
        return instance;
    }
    
    public void initialize(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Mad Machines");
        stage.setWidth(1920);
        stage.setHeight(1080);
    }
    
    public void showMainMenu() {
        loadScene("/fxml/main_menu.fxml");
    }
    
    public void showLevelSelection() {
        loadScene("/fxml/level_selection.fxml");
    }
    
    public void showLevelEditor() {
        loadScene("/fxml/level_editor.fxml");
    }
    
    public void showGameEditor() {
        loadScene("/fxml/game_editor.fxml");
    }
    
    public void showGameEditorWithLevel(String levelFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game_editor.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1920, 1080);
            
            // CSS laden
            try {
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            } catch (Exception e) {
                System.out.println("CSS konnte nicht geladen werden: " + e.getMessage());
            }
            
            // Controller setzen und Level laden
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
            System.err.println("Fehler beim Laden des Game Editors mit Level: " + levelFileName);
            e.printStackTrace();
        }
    }
    
    public void showGame() {
        loadScene("/fxml/game.fxml");
    }
    
    private Controller lastController;
    
    public Controller getLastController() {
        return lastController;
    }
    
    public void showSettings() {
        loadScene("/fxml/settings_menu.fxml");
    }
    
    public void switchToMainMenu() {
        showMainMenu();
    }
    
    private void loadScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1920, 1080);
            
            // CSS laden
            try {
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            } catch (Exception e) {
                System.out.println("CSS konnte nicht geladen werden: " + e.getMessage());
            }
            
            // Controller setzen
            Object controller = loader.getController();
            if (controller instanceof Controller) {
                ((Controller) controller).setViewManager(this);
                lastController = (Controller) controller; // Controller für späteren Zugriff speichern
            }
            
            primaryStage.setScene(scene);
            if (!primaryStage.isShowing()) {
                primaryStage.show();
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Laden der Szene: " + fxmlPath);
            e.printStackTrace();
        }
    }
}
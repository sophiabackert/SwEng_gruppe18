package mm.gui;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Hauptklasse für die grafische Benutzeroberfläche.
 */
public class Gui extends Application {
    private ViewManager viewManager;

    @Override
    public void start(Stage primaryStage) {
        // Setze den ViewManager auf
        viewManager = ViewManager.getInstance();
        viewManager.initialize(primaryStage);
        
        // Starte mit dem Hauptmenü
        viewManager.switchToMainMenu();
    }
    
    /**
     * Startet die GUI-Anwendung.
     */
    public void run() {
        launch();
    }
}

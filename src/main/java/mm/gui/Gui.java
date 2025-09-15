package mm.gui;

import javafx.application.Application;
import javafx.stage.Stage;
import mm.gui.controller.ViewManager;

/**
 * Einstiegspunkt für die JavaFX-GUI-Anwendung.
 * <p>
 * Initialisiert den ViewManager und startet die Hauptanwendung.
 * </p>
 */
public class Gui extends Application {
    private ViewManager viewManager;

    /**
     * Startet die JavaFX-Anwendung und initialisiert die Hauptansicht.
     * @param primaryStage Primärer JavaFX-Stage
     */
    @Override
    public void start(Stage primaryStage) {
        viewManager = ViewManager.getInstance();
        viewManager.initialize(primaryStage);
        
        viewManager.switchToMainMenu();
    }
    
    /**
     * Startet die Anwendung (wird extern aufgerufen).
     */
    public void run() {
        launch();
    }
}

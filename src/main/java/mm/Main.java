package mm;

import javafx.application.Application;
import javafx.stage.Stage;
import mm.gui.ViewManager;

/**
 * Hauptklasse der Anwendung.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Einfache GUI-Initialisierung
        primaryStage.setTitle("Mad Machines");
        primaryStage.setWidth(1280);
        primaryStage.setHeight(720);
        
        ViewManager viewManager = ViewManager.getInstance();
        viewManager.initialize(primaryStage);
        
        // Starte mit dem Hauptmenü (ohne Exception handling)
        viewManager.switchToMainMenu();
    }

    /**
     * Haupteinstiegspunkt der Anwendung.
     */
    public static void main(String[] args) {
        launch(args);
    }
}

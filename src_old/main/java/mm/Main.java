package mm;

import javafx.application.Application;
import javafx.stage.Stage;
import mm.gui.ViewManager;
import mm.gui.NavigationException;

/**
 * Hauptklasse der Anwendung.
 */
public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        ViewManager.getInstance().initialize(primaryStage);
        try {
            ViewManager.getInstance().showMainMenu();
        } catch (NavigationException e) {
            // hier kannst du Fehler-Logging, Dialog o.ä. machen
            e.printStackTrace();
        }
    }

    /**
     * Haupteinstiegspunkt der Anwendung.
     */
    public static void main(String[] args) {
        launch(args);
    }
}

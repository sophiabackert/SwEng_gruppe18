package mm.gui;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Hauptklasse für die grafische Benutzeroberfläche.
 */
public class Gui extends Application {
    private ViewManager viewManager;
    private FPSManager fpsManager;
    private GravityManager gravityManager;

    @Override
    public void start(Stage primaryStage) {
        // Initialisiere die Manager
        fpsManager = FPSManager.getInstance();
        fpsManager.startFPSLimiter();
        AudioManager.getInstance(); // Initialisiere den AudioManager
        gravityManager = GravityManager.getInstance(); // Initialisiere den GravityManager
        
        // Setze den ViewManager auf
        viewManager = ViewManager.getInstance();
        viewManager.initialize(primaryStage);
        
        try {
            // Starte mit dem Hauptmenü
            viewManager.switchToMainMenu();
        } catch (NavigationException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    @Override
    public void stop() {
        // Stoppe den FPS-Limiter
        if (fpsManager != null) {
            fpsManager.stopFPSLimiter();
        }
    }
    
    /**
     * Startet die GUI-Anwendung.
     */
    public void run() {
        launch();
    }
}

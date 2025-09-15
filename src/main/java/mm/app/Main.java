package mm.app;

import javafx.application.Application;
import javafx.stage.Stage;
import mm.gui.controller.ViewManager;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Mad Machines");
        primaryStage.setWidth(1280);
        primaryStage.setHeight(720);
        
        ViewManager viewManager = ViewManager.getInstance();
        viewManager.initialize(primaryStage);
        
        viewManager.switchToMainMenu();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

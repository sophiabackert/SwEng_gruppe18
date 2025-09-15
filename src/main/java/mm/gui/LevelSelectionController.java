package mm.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * The type Level selection controller.
 */
public class LevelSelectionController {

    /**
     * Button level 1 on action.
     *
     * @param event the event
     * @throws IOException the io exception
     */
    public void buttonLevel1OnAction(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/levels/main.fxml"));
        Parent mainRoot = fxmlLoader.load();

        //ich möchte nicht den gespeicherten Spielstand laden
        //GuiController controller = fxmlLoader.getController();
        //controller.loadLevel("level1.json");

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(mainRoot);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

        //scene.getStylesheets().add(getClass().getResource("/assets/main.css").toExternalForm());
        //System.out.println(getClass().getResource("/assets/main.css"));

    }

    /**
     * Back button on action.
     *
     * @param event the event
     * @throws IOException the io exception
     */
    public void backButtonOnAction(ActionEvent event) throws IOException {
        FXMLLoader menuLoader = new FXMLLoader(getClass().getResource("/mm/gui/mainMenu.fxml"));
        Parent menuRoot = menuLoader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(menuRoot);
        stage.setScene(scene);
        stage.show();
    }

    public void Level2OnAction(ActionEvent actionEvent) throws IOException {
        System.out.println("Level 2 starten");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/levels/level2.fxml"));
        Parent level2Root = loader.load();

        //Level2 controller = loader.getController();
        //controller.loadLevel("level2.json");

        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(level2Root);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

    }
}

package mm.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import mm.game.level.LevelStorage;

import java.io.IOException;

public class PauseScreenController {

    @FXML private VBox vBoxPane;

    public void initialize() {
        vBoxPane.setAlignment(Pos.CENTER);

        // Falls du eine AnchorPane als Parent hast:
        AnchorPane.setTopAnchor(vBoxPane, 0.0);
        AnchorPane.setBottomAnchor(vBoxPane, 0.0);
        AnchorPane.setLeftAnchor(vBoxPane, 0.0);
        AnchorPane.setRightAnchor(vBoxPane, 0.0);

    }

    public void backToMenuButton(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mm/gui/mainMenu.fxml"));
        Parent menuRoot = loader.load();

        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(menuRoot);
        stage.setScene(scene);
        stage.show();
    }

    public void restartButtonOnAction(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/levels/main.fxml"));
        Parent game = loader.load();
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(game);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public void continueButton(ActionEvent actionEvent) throws IOException {
        System.out.println("continue");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/levels/main.fxml"));
        Parent gameRoot = loader.load();

        // Controller holen, um die Methode loadGame aufzurufen
        GuiController controller = loader.getController();

        // Automatisch den letzten gespeicherten Spielstand laden
        controller.setLevelToLoad(LevelStorage.getLastLevelFile());
        System.out.println("Level to load: " + LevelStorage.getLastLevelFile()); //debugging print out
        System.out.println("levelToLoad im Controller: " + controller.levelToLoad);
        controller.initAfterLoad();

        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(gameRoot);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();


    }
}

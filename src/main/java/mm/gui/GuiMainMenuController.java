package mm.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.geometry.Pos;

public class GuiMainMenuController {

    @FXML private VBox vboxPane;

    public void initialize() {
        vboxPane.setAlignment(Pos.CENTER);

        AnchorPane.setTopAnchor(vboxPane, 0.0);
        AnchorPane.setBottomAnchor(vboxPane, 0.0);
        AnchorPane.setLeftAnchor(vboxPane, 0.0);
        AnchorPane.setRightAnchor(vboxPane, 0.0);
    }


    public void startButtonOnAction(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/mm/gui/levelSelection.fxml"));
        Parent levelRoot = fxmlLoader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(levelRoot);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    @FXML
    private void exitButtonOnAction() {
        System.out.println("Exit Button wurde gedrückt");
    }

    @FXML
    private void settingsButtonOnAction(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/mm/gui/settings.fxml"));
        Parent settingsRoot = fxmlLoader.load();
        Stage settingsStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene settingsScene = new Scene(settingsRoot);
        settingsStage.setScene(settingsScene);
    }

    @FXML
    private void tutorialButtonOnAction() {
        System.out.println("Tutorial Button wurde gedrückt");
    }

    @FXML
    private void aboutButtonOnAction(ActionEvent about) throws IOException {
        FXMLLoader aboutLoader = new FXMLLoader(getClass().getResource("/mm/gui/about.fxml"));
        Parent aboutRoot = aboutLoader.load();

        Stage aboutStage = (Stage) ((Node) about.getSource()).getScene().getWindow();
        Scene aboutScene = new Scene(aboutRoot);
        aboutStage.setScene(aboutScene);
        aboutStage.show();
    }
}

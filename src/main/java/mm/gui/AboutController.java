package mm.gui;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.TextArea;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

import java.io.IOException;

public class AboutController {

    @FXML private TextArea textArea;
    @FXML private Group aboutScaleGroup;
    @FXML private AnchorPane aboutAnchorPane;
    private final double BASE_WIDTH = 600;
    private final double BASE_HEIGHT = 400;

    @FXML
    public void initialize() {
        setTextArea();
        textArea.setEditable(false);

        Scale scale = new Scale();
        aboutScaleGroup.getTransforms().add(scale);

        // Skalierung an Fenstergröße binden
        aboutAnchorPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            double scaleX = newVal.doubleValue() / BASE_WIDTH;
            double scaleY = aboutAnchorPane.getHeight() / BASE_HEIGHT;
            double scaleFactor = Math.min(scaleX, scaleY); // gleichmäßig skalieren
            scale.setX(scaleFactor);
            scale.setY(scaleFactor);
        });

        aboutAnchorPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            double scaleX = aboutAnchorPane.getWidth() / BASE_WIDTH;
            double scaleY = newVal.doubleValue() / BASE_HEIGHT;
            double scaleFactor = Math.min(scaleX, scaleY); // gleichmäßig skalieren
            scale.setX(scaleFactor);
            scale.setY(scaleFactor);
        });
    }

    public void setTextArea() {
        textArea.setText("Wir sind ein kleines Entwicklerteam mit einer gemeinsamen Vision:\n"
                + "Spiele zu erschaffen, die kreativ herausfordern und Spaß machen.\n"
                + "Unser aktuelles Projekt wurde inspiriert von „Crazy Machines“ – wir wollten eine vereinfachte Version erschaffen,\n"
                + "die clevere Mechanik und zugängliches Gameplay kombiniert. \n"
                + "Mit unserem Spiel möchten wir Spieler:innen die Möglichkeit geben, physikalische Rätsel mit verschiedenen Elementen zu lösen,\n"
                + "eigene Ideen auszuprobieren und spielerisch zu lernen. \n"
                + "\n"
                + "Wir freuen uns über jedes Feedback! \n"
                + "Niklas Fengler & Sophia Backert");
    }

    public void backButtonOnAction(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mm/gui/mainMenu.fxml"));
        Parent menuRoot = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(menuRoot);
        stage.setScene(scene);
        stage.show();
    }
}

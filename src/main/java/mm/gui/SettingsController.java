package mm.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.stage.Stage;
import mm.gui.menu.Settings;

import java.io.IOException;

public class SettingsController {

    @FXML private Slider musikRegulator;
    @FXML private CheckBox muteCheckbox;
    @FXML private Label volumeLabel;

    public void initialize() {
        // Lade gespeicherte Einstellungen
        musikRegulator.setValue(Settings.getVolume());
        muteCheckbox.setSelected(Settings.isMuted());

        // Reagiere dynamisch auf Änderungen
        musikRegulator.valueProperty().addListener((obs, oldVal, newVal) -> {
            Settings.setVolume(newVal.doubleValue());
            updateVolumeLabel();
        });

        muteCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            Settings.setMuted(newVal);
        });

        updateVolumeLabel();
    }

    private void updateVolumeLabel() {
        if (Settings.isMuted()) {
            volumeLabel.setText("Volume: Muted");
        } else {
            volumeLabel.setText("Volume: " + String.format("%.0f", musikRegulator.getValue()));
        }
    }

    public void backButtonOnAction(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mm/gui/mainMenu.fxml"));
        Parent menuRoot = loader.load();

        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(menuRoot);
        stage.setScene(scene);
        stage.show();
    }
}

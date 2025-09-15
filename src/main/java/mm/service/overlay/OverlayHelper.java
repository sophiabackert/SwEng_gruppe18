package mm.service.overlay;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import javafx.animation.PauseTransition;

/**
 * Hilfsklasse zur Anzeige von Overlay-Warnungen im GUI.
 * <p>
 * Zeigt temporäre Warnmeldungen als Overlay auf einem StackPane an.
 * </p>
 */
public class OverlayHelper {
    /**
     * Zeigt eine Warnmeldung als Overlay für eine bestimmte Dauer an.
     * @param parent StackPane, auf dem das Overlay angezeigt wird
     * @param message Die anzuzeigende Nachricht
     * @param duration Dauer in Sekunden
     */
    public static void showWarning(StackPane parent, String message, int duration) {
        Label msg = new Label(message);
        msg.setWrapText(true);
        msg.setMaxWidth(260);
        msg.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;"
        );
        StackPane box = new StackPane(msg);
        box.setPrefWidth(300);
        box.setPrefHeight(100);
        box.setMaxSize(300, 100);
        box.setStyle(
            "-fx-background-color: red;" +
            "-fx-border-color: white;" +
            "-fx-border-width: 3px;" +
            "-fx-background-radius: 10;" +
            "-fx-border-radius: 10;" +
            "-fx-padding: 20;" +
            "-fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 2);"
        );
        box.setMouseTransparent(true);
        StackPane.setAlignment(box, javafx.geometry.Pos.CENTER);
        parent.getChildren().add(box);
        PauseTransition wait = new PauseTransition(Duration.seconds(duration));
        wait.setOnFinished(e -> parent.getChildren().remove(box));
        wait.play();
    }
} 
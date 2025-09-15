package mm.gui.components;

import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

/**
 * Ein schwebender Pause-Button, der in der oberen rechten Ecke des Spiels angezeigt wird.
 */
public class FloatingPauseButton extends Button {
    private static final double BUTTON_SIZE = 40;
    private static final double MARGIN = 10;
    
    /**
     * Erstellt einen neuen schwebenden Pause-Button.
     * @param parent Der Container, in dem der Button platziert wird
     */
    public FloatingPauseButton(Pane parent) {
        // Setze Button-Eigenschaften
        getStyleClass().add("floating-pause-button");
        setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
        setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        setMaxSize(BUTTON_SIZE, BUTTON_SIZE);
        
        // Positioniere den Button in der oberen rechten Ecke
        layoutXProperty().bind(parent.widthProperty().subtract(BUTTON_SIZE + MARGIN));
        setLayoutY(MARGIN);
        
        // Setze den Text
        setText("⏸");
        
        // Setze den Stil
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); " +
                 "-fx-text-fill: white; " +
                 "-fx-background-radius: 20; " +
                 "-fx-font-size: 20; " +
                 "-fx-cursor: hand;");
        
        // Hover-Effekt
        setOnMouseEntered(e -> setStyle(getStyle() + "-fx-background-color: rgba(0, 0, 0, 0.7);"));
        setOnMouseExited(e -> setStyle(getStyle().replace("-fx-background-color: rgba(0, 0, 0, 0.7);",
                                                         "-fx-background-color: rgba(0, 0, 0, 0.5);")));
    }
    
    /**
     * Aktualisiert den Button-Text basierend auf dem Pause-Status.
     * @param isPaused true wenn das Spiel pausiert ist
     */
    public void updateState(boolean isPaused) {
        setText(isPaused ? "▶" : "⏸");
    }
} 
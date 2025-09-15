package mm.gui;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/**
 * Ein Overlay für die Anzeige von Gewinn- und Verlier-Bildschirmen.
 */
public class GameOverOverlay extends VBox {
    private static final double FADE_DURATION = 0.5; // Sekunden
    
    private final Rectangle background;
    private final Text messageText;
    private final Text subtitleText;
    private final Button retryButton;
    private final Button menuButton;
    private Runnable onRetry;
    private Runnable onMenu;
    
    /**
     * Erstellt ein neues GameOverOverlay.
     * @param canvas Das Canvas, auf dem das Overlay angezeigt wird
     */
    public GameOverOverlay(Canvas canvas) {
        // Konfiguriere das Layout
        setAlignment(Pos.CENTER);
        setSpacing(20);
        
        // Erstelle den halbtransparenten Hintergrund
        background = new Rectangle(canvas.getWidth(), canvas.getHeight());
        background.setFill(Color.rgb(0, 0, 0, 0.7));
        
        // Erstelle die Textelemente
        messageText = new Text();
        messageText.setFill(Color.WHITE);
        messageText.setStyle("-fx-font-size: 48px; -fx-font-weight: bold;");
        messageText.setTextAlignment(TextAlignment.CENTER);
        
        subtitleText = new Text();
        subtitleText.setFill(Color.WHITE);
        subtitleText.setStyle("-fx-font-size: 24px;");
        subtitleText.setTextAlignment(TextAlignment.CENTER);
        
        // Erstelle die Buttons
        retryButton = new Button("Erneut versuchen");
        retryButton.setStyle("-fx-font-size: 18px; -fx-padding: 10 20; -fx-background-radius: 5;");
        retryButton.setOnAction(e -> {
            if (onRetry != null) {
                onRetry.run();
            }
        });
        
        menuButton = new Button("Zurück zum Menü");
        menuButton.setStyle("-fx-font-size: 18px; -fx-padding: 10 20; -fx-background-radius: 5;");
        menuButton.setOnAction(e -> {
            if (onMenu != null) {
                onMenu.run();
            }
        });
        
        // Füge alle Elemente hinzu
        getChildren().addAll(background, messageText, subtitleText, retryButton, menuButton);
        
        // Verstecke das Overlay initial
        setVisible(false);
        setMouseTransparent(true);
    }
    
    /**
     * Zeigt den Gewinn-Bildschirm an.
     */
    public void showWinScreen() {
        messageText.setText("Level geschafft!");
        subtitleText.setText("Glückwunsch! Du hast das Level erfolgreich abgeschlossen!");
        show();
    }
    
    /**
     * Zeigt den Verlier-Bildschirm an.
     */
    public void showLoseScreen() {
        messageText.setText("Zeit abgelaufen!");
        subtitleText.setText("Leider verloren! Versuche es noch einmal!");
        show();
    }
    
    /**
     * Zeigt das Overlay mit einer Fade-Animation an.
     */
    private void show() {
        setVisible(true);
        setMouseTransparent(false);
        
        // Erstelle eine Fade-In-Animation
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(FADE_DURATION), this);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
    
    /**
     * Versteckt das Overlay mit einer Fade-Animation.
     */
    public void hide() {
        // Erstelle eine Fade-Out-Animation
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(FADE_DURATION), this);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            setVisible(false);
            setMouseTransparent(true);
        });
        fadeOut.play();
    }
    
    /**
     * Setzt den Callback für den "Erneut versuchen"-Button.
     * @param callback Der Callback
     */
    public void setOnRetry(Runnable callback) {
        this.onRetry = callback;
    }
    
    /**
     * Setzt den Callback für den "Zurück zum Menü"-Button.
     * @param callback Der Callback
     */
    public void setOnMenu(Runnable callback) {
        this.onMenu = callback;
    }
} 
package mm.gui;

import javafx.animation.FadeTransition;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/**
 * Overlay für die Anzeige des Pause-Zustands.
 */
public class PauseOverlay {
    private final Canvas canvas;
    private final GraphicsContext gc;
    private boolean visible;
    
    /**
     * Erstellt ein neues PauseOverlay.
     * @param canvas Das Canvas, auf dem der Overlay gezeichnet wird
     */
    public PauseOverlay(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.visible = false;
    }
    
    /**
     * Zeigt den Pause-Overlay an.
     */
    public void show() {
        visible = true;
        
        // Halbtransparenter schwarzer Hintergrund
        gc.setGlobalAlpha(0.5);
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // "PAUSE" Text
        gc.setGlobalAlpha(1.0);
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.setFont(Font.font("Arial", 60));
        gc.setTextAlign(TextAlignment.CENTER);
        
        double x = canvas.getWidth() / 2;
        double y = canvas.getHeight() / 2;
        
        gc.strokeText("PAUSE", x, y);
        gc.fillText("PAUSE", x, y);
        
        // Fade-Effekt
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.3), canvas);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
    
    /**
     * Versteckt den Pause-Overlay.
     */
    public void hide() {
        if (!visible) return;
        
        visible = false;
        
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.3), canvas);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight()));
        fadeOut.play();
    }
    
    /**
     * Prüft, ob der Overlay sichtbar ist.
     * @return true wenn der Overlay sichtbar ist
     */
    public boolean isVisible() {
        return visible;
    }
} 
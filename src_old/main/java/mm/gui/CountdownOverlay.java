package mm.gui;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/**
 * Overlay für die Anzeige des 3-2-1 Countdowns vor Spielstart.
 */
public class CountdownOverlay {
    private final Canvas canvas;
    private final GraphicsContext gc;
    private int currentNumber;
    private Timeline countdownTimeline;
    private Runnable onCountdownFinished;
    
    /**
     * Erstellt ein neues CountdownOverlay.
     * @param canvas Das Canvas, auf dem der Countdown gezeichnet wird
     */
    public CountdownOverlay(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.currentNumber = 3;
    }
    
    /**
     * Startet den Countdown.
     * @param onFinished Wird aufgerufen, wenn der Countdown beendet ist
     */
    public void startCountdown(Runnable onFinished) {
        this.onCountdownFinished = onFinished;
        this.currentNumber = 3;
        
        // Erstelle die Timeline für den Countdown
        countdownTimeline = new Timeline();
        
        // Füge KeyFrames für jeden Countdown-Schritt hinzu
        for (int i = 0; i <= 3; i++) {
            int number = 3 - i;
            KeyFrame frame = new KeyFrame(Duration.seconds(i), e -> {
                if (number > 0) {
                    showNumber(number);
                } else {
                    hideNumber();
                    if (onCountdownFinished != null) {
                        onCountdownFinished.run();
                    }
                }
            });
            countdownTimeline.getKeyFrames().add(frame);
        }
        
        // Starte den Countdown
        countdownTimeline.play();
    }
    
    /**
     * Zeigt eine Zahl im Countdown an.
     * @param number Die anzuzeigende Zahl
     */
    private void showNumber(int number) {
        this.currentNumber = number;
        
        // Lösche den vorherigen Inhalt
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // Setze die Schriftart und -größe
        gc.setFont(Font.font("Arial", 120));
        gc.setTextAlign(TextAlignment.CENTER);
        
        // Zeichne die Zahl mit Fade-Effekt
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.2), canvas);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        
        // Zeichne die Zahl
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(3);
        double x = canvas.getWidth() / 2;
        double y = canvas.getHeight() / 2 + 40; // +40 für vertikale Zentrierung
        gc.strokeText(String.valueOf(number), x, y);
        gc.fillText(String.valueOf(number), x, y);
        
        fadeIn.play();
    }
    
    /**
     * Versteckt die aktuelle Zahl.
     */
    private void hideNumber() {
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.2), canvas);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight()));
        fadeOut.play();
    }
    
    /**
     * Stoppt den Countdown.
     */
    public void stopCountdown() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
} 
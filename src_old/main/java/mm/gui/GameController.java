package mm.gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import mm.engine.GameEngine;
import mm.model.LevelInfo;
import mm.world.World;

/**
 * Controller für die Hauptspielansicht (Game Simulator).
 */
public class GameController extends GuiController {
    
    @FXML private BorderPane mainContainer;
    @FXML private StackPane gameArea;
    @FXML private Canvas gameCanvas;
    @FXML private Button exitButton;
    @FXML private Label taskLabel;
    @FXML private Label timeLabel;
    @FXML private Button pauseButton;
    
    @FXML private StackPane overlayContainer;
    @FXML private VBox countdownOverlay;
    @FXML private Label countdownLabel;
    @FXML private VBox pauseOverlay;
    @FXML private VBox winOverlay;
    @FXML private VBox gameOverOverlay;
    
    private GameEngine gameEngine;
    private World gameWorld;
    private LevelInfo currentLevel;
    private Timeline countdownTimer;
    private Timeline gameTimer;
    private double remainingTime = 60.0;
    private boolean isPaused = false;
    private boolean gameStarted = false;
    
    /**
     * Initialisiert das Spiel mit einem Level.
     */
    public void initializeGame(LevelInfo level) {
        this.currentLevel = level;
        this.remainingTime = 60.0;
        this.isPaused = false;
        this.gameStarted = false;
        
        // Aufgabe anzeigen
        taskLabel.setText(level.getDescription());
        
        // Canvas an Fenstergröße anpassen
        setupCanvasBinding();
        
        try {
            // Welt initialisieren
            gameWorld = new World();
            // Level laden würde hier stattfinden
            // gameWorld.loadFromFile(level.getFileName());
            
            // Spiel-Engine initialisieren
            gameEngine = new GameEngine(gameCanvas, gameWorld);
            
            // Callbacks setzen
            gameEngine.setOnGameWon(this::handleGameWon);
            gameEngine.setOnGameOver(this::handleGameOver);
            
            // Countdown starten
            startCountdown();
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Fehler beim Laden", "Das Level konnte nicht geladen werden: " + e.getMessage());
        }
    }
    
    /**
     * Bindet das Canvas an die Fenstergröße.
     */
    private void setupCanvasBinding() {
        gameCanvas.widthProperty().bind(gameArea.widthProperty());
        gameCanvas.heightProperty().bind(gameArea.heightProperty());
    }
    
    /**
     * Startet den Countdown vor dem Spiel.
     */
    private void startCountdown() {
        overlayContainer.setVisible(true);
        countdownOverlay.setVisible(true);
        
        final int[] count = {3};
        countdownLabel.setText(String.valueOf(count[0]));
        
        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            count[0]--;
            if (count[0] > 0) {
                countdownLabel.setText(String.valueOf(count[0]));
            } else {
                countdownLabel.setText("START!");
                Timeline startDelay = new Timeline(new KeyFrame(Duration.millis(500), event -> {
                    hideAllOverlays();
                    startGame();
                }));
                startDelay.play();
                countdownTimer.stop();
            }
        }));
        countdownTimer.setCycleCount(3);
        countdownTimer.play();
    }
    
    /**
     * Startet das eigentliche Spiel.
     */
    private void startGame() {
        gameStarted = true;
        
        if (gameEngine != null) {
            gameEngine.setReady();
            gameEngine.start();
        }
        
        // Game Timer starten
        startGameTimer();
    }
    
    /**
     * Startet den Spiel-Timer.
     */
    private void startGameTimer() {
        gameTimer = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            if (!isPaused && gameStarted) {
                remainingTime -= 0.1;
                updateTimeDisplay();
                
                if (remainingTime <= 0) {
                    remainingTime = 0;
                    updateTimeDisplay();
                    handleGameOver();
                }
            }
        }));
        gameTimer.setCycleCount(Timeline.INDEFINITE);
        gameTimer.play();
    }
    
    /**
     * Aktualisiert die Zeitanzeige.
     */
    private void updateTimeDisplay() {
        Platform.runLater(() -> {
            timeLabel.setText(String.format("%.1f", remainingTime));
        });
    }
    
    /**
     * Behandelt das Gewinnen des Spiels.
     */
    private void handleGameWon() {
        Platform.runLater(() -> {
            stopGame();
            overlayContainer.setVisible(true);
            winOverlay.setVisible(true);
        });
    }
    
    /**
     * Behandelt das Verlieren des Spiels.
     */
    private void handleGameOver() {
        Platform.runLater(() -> {
            stopGame();
            overlayContainer.setVisible(true);
            gameOverOverlay.setVisible(true);
        });
    }
    
    /**
     * Stoppt das Spiel.
     */
    private void stopGame() {
        gameStarted = false;
        if (gameTimer != null) {
            gameTimer.stop();
        }
        if (gameEngine != null) {
            // Game Engine pausieren
        }
    }
    
    /**
     * Versteckt alle Overlays.
     */
    private void hideAllOverlays() {
        overlayContainer.setVisible(false);
        countdownOverlay.setVisible(false);
        pauseOverlay.setVisible(false);
        winOverlay.setVisible(false);
        gameOverOverlay.setVisible(false);
    }
    
    @FXML
    private void onExitGame() {
        try {
            stopGame();
            // Zurück zum Game Editor (nicht implementiert) oder Level Selection
            getViewManager().showLevelSelection();
        } catch (NavigationException e) {
            showError("Navigation fehlgeschlagen", e.getMessage());
        }
    }
    
    @FXML
    private void onPauseToggle() {
        if (!gameStarted) return;
        
        isPaused = !isPaused;
        
        if (isPaused) {
            pauseButton.setText("▶");
            overlayContainer.setVisible(true);
            pauseOverlay.setVisible(true);
            if (gameEngine != null) {
                gameEngine.pause();
            }
        } else {
            pauseButton.setText("⏸");
            hideAllOverlays();
            // Game Engine fortsetzen
        }
    }
    
    @FXML
    private void onResumeGame() {
        isPaused = false;
        pauseButton.setText("⏸");
        hideAllOverlays();
        // Game Engine fortsetzen
    }
    
    @FXML
    private void onPlayAgain() {
        hideAllOverlays();
        // Spiel neu starten
        if (currentLevel != null) {
            initializeGame(currentLevel);
        }
    }
    
    @FXML
    private void onBackToLevelSelection() {
        try {
            stopGame();
            getViewManager().showLevelSelection();
        } catch (NavigationException e) {
            showError("Navigation fehlgeschlagen", e.getMessage());
        }
    }
    
    /**
     * Zeigt eine Fehlermeldung an.
     */
    private void showError(String title, String message) {
        // Implementierung für Fehlerdialog
        System.err.println(title + ": " + message);
    }
} 
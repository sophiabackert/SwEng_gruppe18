package mm.gui.controller;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import mm.domain.editor.PlacedObject;
import mm.service.rendering.GameRenderer;
import mm.service.physics.PhysicsManager;
import java.util.List;

/**
 * Controller für die Spielansicht (GameView) im Spielmodus.
 * <p>
 * Steuert die Anzeige, das Timing, die Physik und die Benutzerinteraktion während eines Spiels.
 * Verwaltet Overlays (Countdown, Pause, Win, Game Over) und die Kommunikation mit dem ViewManager.
 * </p>
 */
public class GameController extends Controller {
    /** Canvas für die Spielfeldanzeige */
    @FXML private Canvas gameCanvas;
    /** Label für die Aufgabenbeschreibung */
    @FXML private Label taskLabel;
    /** Label für die verbleibende Zeit */
    @FXML private Label timeLabel;
    /** Button zum Pausieren */
    @FXML private Button pauseButton;
    /** Button zum Verlassen */
    @FXML private Button exitButton;
    /** Overlay-Container für Countdown, Pause, Win, Game Over */
    @FXML private StackPane overlayContainer;
    @FXML private VBox countdownOverlay;
    @FXML private VBox pauseOverlay;
    @FXML private VBox winOverlay;
    @FXML private VBox gameOverOverlay;
    @FXML private Label countdownLabel;

    /** Haupt-Spielschleife (AnimationTimer) */
    private AnimationTimer gameLoop;
    /** Gibt an, ob das Spiel pausiert ist */
    private boolean isPaused = false;
    /** Verbleibende Spielzeit in Sekunden */
    private double gameTime = 60.0;
    /** Countdown-Zeit in Sekunden */
    private double countdownTime = 3.0;
    /** Gibt an, ob der Countdown angezeigt wird */
    private boolean showingCountdown = true;
    /** Renderer für die Spielfeldanzeige */
    private GameRenderer gameRenderer;
    /** Physikmanager für die Spielwelt */
    private PhysicsManager physicsManager;

    /**
     * Initialisiert den Controller und die Spiellogik.
     * Wird automatisch von JavaFX nach dem Laden des FXML aufgerufen.
     */
    @FXML
    private void initialize() {
        setupKeyControls();
        gameRenderer = new GameRenderer(gameCanvas);
        physicsManager = new PhysicsManager(gameCanvas);
        physicsManager.setOnGameWon(this::showWinOverlay);
    }

    /**
     * Initialisiert das Spiel mit den gegebenen Objekten und dem Ziel.
     * @param playerObjects Vom Spieler platzierte Objekte
     * @param levelObjects Level-Objekte
     * @param objective Zielbeschreibung
     */
    public void initializeGame(List<PlacedObject> playerObjects, List<PlacedObject> levelObjects, String objective) {
        taskLabel.setText(objective);
        for (PlacedObject po : levelObjects) {
            physicsManager.addObjectToWorld(po, true);
        }
        for (PlacedObject po : playerObjects) {
            physicsManager.addObjectToWorld(po, false);
        }
        physicsManager.createWorldBounds();
        startGame();
    }

    /**
     * Startet die Spielschleife und den Countdown.
     */
    private void startGame() {
        SettingsController.resetFPSTimer();
        showCountdown();
        
        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;
            private long lastCountdownUpdate = 0;
            
            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    lastCountdownUpdate = now;
                    render();
                    return;
                }
                
                double deltaTime = (now - lastUpdate) * 1e-9;
                
                if (showingCountdown) {
                    double countdownDelta = (now - lastCountdownUpdate) * 1e-9;
                    lastCountdownUpdate = now;
                    
                    countdownTime -= countdownDelta;
                    if (countdownTime <= 0) {
                        hideCountdown();
                    }
                    
                    Platform.runLater(() -> {
                        if (countdownTime > 0) {
                            countdownLabel.setText(String.valueOf((int) Math.ceil(countdownTime)));
                        } else {
                            countdownLabel.setText("LOS!");
                        }
                    });
                    
                    render();
                    return;
                }
                
                if (!SettingsController.shouldUpdate()) {
                    return;
                }
                
                lastUpdate = now;
                
                if (!isPaused) {
                    double scaledDeltaTime = deltaTime * SettingsController.getTimeScale();
                    update(scaledDeltaTime);
                }
                
                render();
            }
        };
        gameLoop.start();
    }

    /**
     * Aktualisiert die Spielzeit und Physik.
     * @param deltaTime Zeitdifferenz in Sekunden
     */
    private void update(double deltaTime) {
        gameTime -= deltaTime;
        if (gameTime <= 0) {
            gameTime = 0;
            showGameOverOverlay();
        }
        
        Platform.runLater(() -> {
            timeLabel.setText(String.format("%.1f", gameTime));
        });
        
        physicsManager.step();
        physicsManager.applyBalloonBuoyancy();
    }

    /**
     * Rendert das Spielfeld.
     */
    private void render() {
        gameRenderer.render(physicsManager.getBodies());
    }

    /**
     * Setzt die Tastenkürzel für Pause und Neustart.
     */
    private void setupKeyControls() {
        gameCanvas.setFocusTraversable(true);
        gameCanvas.setOnKeyPressed(this::handleKeyPressed);
    }

    /**
     * Behandelt Tastendrücke (Pause, Restart).
     * @param event Das KeyEvent
     */
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.SPACE) {
            handlePause();
        } else if (event.getCode() == KeyCode.R) {
            handleRestart();
        }
    }
    
    /**
     * Pausiert das Spiel und zeigt das Pause-Overlay an.
     */
    @FXML
    private void handlePause() {
        if (!isPaused) {
            isPaused = true;
            showPauseOverlay();
        }
    }

    /**
     * Setzt das Spiel nach einer Pause fort und blendet das Pause-Overlay aus.
     */
    @FXML
    private void handleResume() {
        isPaused = false;
        hidePauseOverlay();
    }

    /**
     * Startet das Spiel neu und stellt den Editor-Zustand wieder her.
     */
    @FXML
    private void handleRestart() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (viewManager != null) {
            viewManager.showGameEditor();
            Object controller = viewManager.getLastController();
            if (controller instanceof GameEditorController) {
                GameEditorController gameEditorController = (GameEditorController) controller;
                gameEditorController.restoreState();
            }
        }
    }

    /**
     * Beendet das Spiel und kehrt zum Editor zurück.
     */
    @FXML
    private void handleBack() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (viewManager != null) {
            viewManager.showGameEditor();
            Object controller = viewManager.getLastController();
            if (controller instanceof GameEditorController) {
                GameEditorController gameEditorController = (GameEditorController) controller;
                gameEditorController.restoreState();
            }
        }
    }

    /**
     * Beendet das Spiel und öffnet die Levelauswahl.
     */
    @FXML
    private void handleToLevelSelection() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (viewManager != null) {
            viewManager.showLevelSelection();
        }
    }

    /**
     * Zeigt das Countdown-Overlay an.
     */
    private void showCountdown() {
        overlayContainer.setVisible(true);
        countdownOverlay.setVisible(true);
        showingCountdown = true;
        countdownTime = 3.0;
    }

    /**
     * Blendet das Countdown-Overlay aus.
     */
    private void hideCountdown() {
        countdownOverlay.setVisible(false);
        overlayContainer.setVisible(false);
        showingCountdown = false;
    }

    /**
     * Zeigt das Pause-Overlay an.
     */
    private void showPauseOverlay() {
        overlayContainer.setVisible(true);
        pauseOverlay.setVisible(true);
    }

    /**
     * Blendet das Pause-Overlay aus.
     */
    private void hidePauseOverlay() {
        pauseOverlay.setVisible(false);
        overlayContainer.setVisible(false);
    }

    /**
     * Zeigt das Win-Overlay an.
     */
    private void showWinOverlay() {
        overlayContainer.setVisible(true);
        winOverlay.setVisible(true);
    }

    /**
     * Zeigt das Game-Over-Overlay an.
     */
    private void showGameOverOverlay() {
        overlayContainer.setVisible(true);
        gameOverOverlay.setVisible(true);
    }
    

} 
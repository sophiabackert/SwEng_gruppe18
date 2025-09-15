package mm.engine;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import mm.objects.GameObject;
import mm.objects.zones.GoalZone;
import mm.objects.balls.Ball;
import mm.world.World;
import org.jbox2d.common.Vec2;
import mm.gui.CountdownOverlay;
import mm.gui.PauseOverlay;
import java.util.Optional;

/**
 * Die Hauptspiel-Engine, die das Spiel verwaltet und aktualisiert.
 */
public class GameEngine {
    public enum GameState {
        LOADING,
        READY,
        COUNTDOWN,
        RUNNING,
        PAUSED,
        GAME_OVER,
        GAME_WON
    }

    private static final float PHYSICS_TIME_STEP = 1.0f / 60.0f;
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;
    private static final double GAME_TIME_LIMIT = 60.0; // 60 Sekunden Zeitlimit
    
    private final Canvas gameCanvas;
    private final World gameWorld;
    private final GameRenderer gameRenderer;
    private final CollisionManager collisionManager;
    private final CountdownOverlay countdownOverlay;
    private final PauseOverlay pauseOverlay;
    private GameState currentState;
    private AnimationTimer gameLoop;
    private long lastUpdateTime;
    private GoalZone goalZone;
    private Runnable onGameWon;
    private Runnable onGameOver;
    private double gameTime; // Aktuelle Spielzeit in Sekunden
    
    /**
     * Erstellt eine neue GameEngine.
     * @param canvas Das Canvas für das Rendering
     * @param world Die Spielwelt
     */
    public GameEngine(Canvas canvas, World world) {
        if (canvas == null) {
            throw new IllegalArgumentException("Canvas darf nicht null sein");
        }
        if (world == null) {
            throw new IllegalArgumentException("World darf nicht null sein");
        }
        
        this.gameCanvas = canvas;
        this.gameWorld = world;
        this.gameRenderer = new GameRenderer(canvas, world);
        this.collisionManager = new CollisionManager();
        this.countdownOverlay = new CountdownOverlay(canvas);
        this.pauseOverlay = new PauseOverlay(canvas);
        this.currentState = GameState.LOADING;
        this.gameTime = 0.0;
        
        // Initialisiere die Physik-Welt
        var physicsWorld = gameWorld.getPhysicsWorld();
        if (physicsWorld != null) {
            physicsWorld.setContactListener(collisionManager);
        }
        
        // Erstelle die Zielzone
        createGoalZone();
        
        // Initialisiere die Spielschleife
        initGameLoop();
    }
    
    /**
     * Fügt ein GameObject zum Spiel hinzu.
     * @param object Das hinzuzufügende GameObject
     */
    public void addGameObject(GameObject object) {
        gameWorld.addGameObject(object);
    }
    
    /**
     * Entfernt ein GameObject aus dem Spiel.
     * @param object Das zu entfernende GameObject
     */
    public void removeGameObject(GameObject object) {
        gameWorld.removeGameObject(object);
    }
    
    /**
     * Erstellt die Zielzone.
     */
    private void createGoalZone() {
        // Position und Größe der Zielzone (anpassen nach Bedarf)
        Vec2 position = new Vec2(8.0f, 1.0f); // Beispielposition
        float width = 1.0f;
        float height = 1.0f;
        
        // Übergebe die physikalische Welt an die GoalZone
        goalZone = new GoalZone(gameWorld.getPhysicsWorld(), position, width, height, this::handleGoalReached);
        gameWorld.addGameObject(goalZone);
    }
    
    /**
     * Wird aufgerufen, wenn ein Ball die Zielzone erreicht.
     */
    private void handleGoalReached() {
        if (currentState == GameState.RUNNING) {
            setGameState(GameState.GAME_WON);
            if (onGameWon != null) {
                onGameWon.run();
            }
        }
    }
    
    /**
     * Setzt den Callback für das Gewinnen des Spiels.
     * @param callback Der Callback
     */
    public void setOnGameWon(Runnable callback) {
        this.onGameWon = callback;
    }
    
    /**
     * Setzt den Callback für das Verlieren des Spiels.
     * @param callback Der Callback
     */
    public void setOnGameOver(Runnable callback) {
        this.onGameOver = callback;
    }
    
    /**
     * Gibt die verbleibende Spielzeit in Sekunden zurück.
     * @return Die verbleibende Zeit
     */
    public double getRemainingTime() {
        return Math.max(0.0, GAME_TIME_LIMIT - gameTime);
    }
    
    /**
     * Initialisiert die Spielschleife.
     */
    private void initGameLoop() {
        this.gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (currentState == GameState.RUNNING) {
                    // Berechne die vergangene Zeit
                    if (lastUpdateTime == 0) {
                        lastUpdateTime = now;
                        return;
                    }
                    
                    // Berechne die vergangene Zeit in Sekunden
                    double deltaTime = (now - lastUpdateTime) / 1_000_000_000.0;
                    lastUpdateTime = now;
                    
                    // Aktualisiere die Spielzeit
                    gameTime += deltaTime;
                    
                    // Prüfe auf Zeitüberschreitung
                    if (gameTime >= GAME_TIME_LIMIT) {
                        gameOver();
                        return;
                    }
                    
                    // Aktualisiere die Physik
                    gameWorld.getPhysicsWorld().step(PHYSICS_TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
                    
                    // Aktualisiere die GameObjects
                    for (var obj : gameWorld.getGameObjects()) {
                        if (obj.isActive()) {
                            obj.update(PHYSICS_TIME_STEP);
                        }
                    }
                    
                    // Rendere die Szene
                    gameRenderer.render();
                }
            }
        };
    }
    
    /**
     * Startet das Spiel mit Countdown.
     */
    public void start() {
        if (currentState == GameState.READY) {
            gameTime = 0.0; // Setze die Spielzeit zurück
            currentState = GameState.COUNTDOWN;
            countdownOverlay.startCountdown(() -> {
                currentState = GameState.RUNNING;
                gameLoop.start();
            });
        } else if (currentState == GameState.PAUSED) {
            currentState = GameState.RUNNING;
            pauseOverlay.hide();
            gameLoop.start();
        }
    }
    
    /**
     * Pausiert das Spiel.
     */
    public void pause() {
        if (currentState == GameState.RUNNING) {
            currentState = GameState.PAUSED;
            gameLoop.stop();
            pauseOverlay.show();
        }
    }
    
    /**
     * Beendet das Spiel.
     */
    public void gameOver() {
        currentState = GameState.GAME_OVER;
        gameLoop.stop();
        if (onGameOver != null) {
            onGameOver.run();
        }
    }
    
    /**
     * Setzt das Spiel in den bereiten Zustand.
     */
    public void setReady() {
        currentState = GameState.READY;
    }
    
    /**
     * Gibt die Spielwelt zurück.
     * @return Die Spielwelt
     */
    public World getWorld() {
        return gameWorld;
    }
    
    /**
     * Gibt den GameRenderer zurück.
     * @return Der GameRenderer
     */
    public GameRenderer getRenderer() {
        return gameRenderer;
    }
    
    /**
     * Gibt den aktuellen Spielzustand zurück.
     * @return Der aktuelle Spielzustand
     */
    public GameState getGameState() {
        return currentState;
    }
    
    /**
     * Setzt den Spielzustand.
     * @param newState Der neue Zustand
     */
    public void setGameState(GameState newState) {
        GameState oldState = currentState;
        currentState = newState;
        
        // Führe zustandsspezifische Aktionen aus
        switch (newState) {
            case COUNTDOWN:
                // Wird vom CountdownOverlay verwaltet
                break;
            case RUNNING:
                if (oldState == GameState.COUNTDOWN) {
                    // Spiel startet nach dem Countdown
                    goalZone.setActive(true);
                }
                break;
            case PAUSED:
                // Physik-Simulation wird in der Spielschleife pausiert
                break;
            case GAME_OVER:
            case GAME_WON:
                goalZone.setActive(false);
                break;
            default:
                break;
        }
    }
    
    /**
     * Setzt das Spiel zurück.
     */
    public void reset() {
        gameLoop.stop();
        gameTime = 0.0;
        
        // Setze alle Spielobjekte zurück
        for (GameObject obj : gameWorld.getObjects()) {
            obj.reset();
        }
        
        // Setze den Spielzustand zurück
        setGameState(GameState.READY);
    }
    
    /**
     * Aktualisiert den Spielzustand.
     */
    private void updateGameState() {
        if (currentState == GameState.RUNNING) {
            // Aktualisiere die Spielzeit
            gameTime += PHYSICS_TIME_STEP;
            
            // Prüfe auf Zeitüberschreitung
            if (gameTime >= GAME_TIME_LIMIT) {
                currentState = GameState.GAME_OVER;
                if (onGameOver != null) {
                    onGameOver.run();
                }
                return;
            }
            
            // Prüfe auf Gewinnbedingung
            if (checkWinCondition()) {
                currentState = GameState.GAME_WON;
                if (onGameWon != null) {
                    onGameWon.run();
                }
                return;
            }
            
            // Aktualisiere die Physik-Simulation
            gameWorld.getPhysicsWorld().step(PHYSICS_TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
        }
    }
    
    /**
     * Prüft, ob die Gewinnbedingung erfüllt ist.
     * @return true, wenn das Spiel gewonnen wurde
     */
    private boolean checkWinCondition() {
        // Suche nach der GoalZone
        Optional<GameObject> goalZone = gameWorld.getObjects().stream()
            .filter(obj -> obj instanceof GoalZone)
            .findFirst();
            
        if (goalZone.isPresent()) {
            // Suche nach einem Ball in der GoalZone
            return gameWorld.getObjects().stream()
                .filter(obj -> obj instanceof Ball)
                .anyMatch(ball -> ((GoalZone) goalZone.get()).containsObject(ball));
        }
        
        return false;
    }
} 
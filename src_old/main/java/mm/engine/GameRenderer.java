package mm.engine;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import mm.world.World;

/**
 * Verantwortlich für das Rendering der Spielwelt.
 */
public class GameRenderer {
    private final Canvas canvas;
    private final World world;
    private final GraphicsContext gc;
    
    /**
     * Erstellt einen neuen GameRenderer.
     * @param canvas Das Canvas für das Rendering
     * @param world Die zu rendernde Spielwelt
     */
    public GameRenderer(Canvas canvas, World world) {
        this.canvas = canvas;
        this.world = world;
        this.gc = canvas.getGraphicsContext2D();
    }
    
    /**
     * Rendert die Spielwelt.
     */
    public void render() {
        // Lösche den Canvas
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // Rendere die Spielwelt
        world.render(gc);
    }
    
    /**
     * Gibt das Canvas zurück.
     * @return Das Canvas
     */
    public Canvas getCanvas() {
        return canvas;
    }
} 
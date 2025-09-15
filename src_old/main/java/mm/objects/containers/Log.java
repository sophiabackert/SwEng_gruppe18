package mm.objects.containers;

import org.jbox2d.common.Vec2;
import javafx.scene.paint.Color;

/**
 * Log implementation with high density and moderate bounce.
 */
public class Log extends Container {
    
    // Log specific constants
    private static final float DEFAULT_WIDTH = 1.0f;  // 1m width
    private static final float DEFAULT_HEIGHT = 0.2f; // 20cm height
    private static final float DEFAULT_DENSITY = 1.5f;  // Dense wood
    private static final float DEFAULT_RESTITUTION = 0.2f; // Low bounce
    private static final float DEFAULT_FRICTION = 0.7f; // High friction
    
    /**
     * Creates a log at the specified position with default properties.
     *
     * @param position Initial position of the log
     */
    public Log(Vec2 position) {
        super(position, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_DENSITY, DEFAULT_RESTITUTION, DEFAULT_FRICTION);
        
        // Set log specific rendering (dark brown color)
        getRenderComponent().setFillColor(Color.SADDLEBROWN);
    }
    
    /**
     * Creates a log with custom properties.
     *
     * @param position Initial position
     * @param width Width in meters
     * @param height Height in meters
     * @param density Mass density
     * @param restitution Bounciness
     * @param friction Friction coefficient
     */
    public Log(Vec2 position, float width, float height, float density, float restitution, float friction) {
        super(position, width, height, density, restitution, friction);
        getRenderComponent().setFillColor(Color.SADDLEBROWN);
    }
}
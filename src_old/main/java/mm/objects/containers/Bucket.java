package mm.objects.containers;

import org.jbox2d.common.Vec2;
import javafx.scene.paint.Color;

/**
 * Bucket implementation with high density and low bounce.
 */
public class Bucket extends Container {
    
    // Bucket specific constants
    private static final float DEFAULT_WIDTH = 0.4f;  // 40cm width
    private static final float DEFAULT_HEIGHT = 0.6f; // 60cm height
    private static final float DEFAULT_DENSITY = 2.0f;  // Heavy
    private static final float DEFAULT_RESTITUTION = 0.1f; // Very low bounce
    private static final float DEFAULT_FRICTION = 0.8f; // High friction
    
    /**
     * Creates a bucket at the specified position with default properties.
     *
     * @param position Initial position of the bucket
     */
    public Bucket(Vec2 position) {
        super(position, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_DENSITY, DEFAULT_RESTITUTION, DEFAULT_FRICTION);
        
        // Set bucket specific rendering (gray color)
        getRenderComponent().setFillColor(Color.GRAY);
    }
    
    /**
     * Creates a bucket with custom properties.
     *
     * @param position Initial position
     * @param width Width in meters
     * @param height Height in meters
     * @param density Mass density
     * @param restitution Bounciness
     * @param friction Friction coefficient
     */
    public Bucket(Vec2 position, float width, float height, float density, float restitution, float friction) {
        super(position, width, height, density, restitution, friction);
        getRenderComponent().setFillColor(Color.GRAY);
    }
}
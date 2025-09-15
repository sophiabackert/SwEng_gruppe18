package mm.objects.containers;

import org.jbox2d.common.Vec2;
import javafx.scene.paint.Color;

/**
 * Plank implementation with moderate density and low bounce.
 */
public class Plank extends Container {
    
    // Plank specific constants
    private static final float DEFAULT_WIDTH = 1.5f;  // 1.5m width
    private static final float DEFAULT_HEIGHT = 0.1f; // 10cm height
    private static final float DEFAULT_DENSITY = 0.7f;  // Light wood
    private static final float DEFAULT_RESTITUTION = 0.2f; // Low bounce
    private static final float DEFAULT_FRICTION = 0.5f; // Moderate friction
    
    /**
     * Creates a plank at the specified position with default properties.
     *
     * @param position Initial position of the plank
     */
    public Plank(Vec2 position) {
        super(position, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_DENSITY, DEFAULT_RESTITUTION, DEFAULT_FRICTION);
        
        // Set plank specific rendering (light brown color)
        getRenderComponent().setFillColor(Color.BURLYWOOD);
    }
    
    /**
     * Creates a plank with custom properties.
     *
     * @param position Initial position
     * @param width Width in meters
     * @param height Height in meters
     * @param density Mass density
     * @param restitution Bounciness
     * @param friction Friction coefficient
     */
    public Plank(Vec2 position, float width, float height, float density, float restitution, float friction) {
        super(position, width, height, density, restitution, friction);
        getRenderComponent().setFillColor(Color.BURLYWOOD);
    }
} 
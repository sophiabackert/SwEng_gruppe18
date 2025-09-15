package mm.objects.balls;

import javafx.scene.paint.Color;
import org.jbox2d.common.Vec2;

/**
 * Repräsentiert einen Billardball im Spiel.
 */
public class BilliardBall extends Ball {
    // Standardwerte für einen Billardball
    private static final float RADIUS = 0.029f;      // 29mm Radius
    private static final float DENSITY = 1.8f;       // Sehr dicht
    private static final float FRICTION = 0.1f;      // Sehr glatt
    private static final float RESTITUTION = 0.95f;  // Sehr elastisch
    private static final Color COLOR = Color.WHITE;

    public BilliardBall(Vec2 position) {
        super(position, RADIUS, DENSITY, FRICTION, RESTITUTION, COLOR);
    }
    
    public BilliardBall(Vec2 position, float radius, float density, float friction, float restitution) {
        super(position, radius, density, friction, restitution, COLOR);
    }

    @Override
    protected String getSoundPrefix() {
        return "billiard";
    }
    
    @Override
    protected float getDefaultRadius() {
        return RADIUS;
    }
    
    @Override
    protected float getDefaultDensity() {
        return DENSITY;
    }
    
    @Override
    protected float getDefaultFriction() {
        return FRICTION;
    }
    
    @Override
    protected float getDefaultRestitution() {
        return RESTITUTION;
    }
    
    @Override
    protected Color getDefaultColor() {
        return COLOR;
    }
}
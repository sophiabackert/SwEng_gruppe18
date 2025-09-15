package mm.objects.balls;

import javafx.scene.paint.Color;
import org.jbox2d.common.Vec2;

/**
 * Implementiert einen Bowlingball mit spezifischen Eigenschaften.
 */
public class BowlingBall extends Ball {
    
    private static final float DEFAULT_RADIUS = 0.5f;
    private static final float DEFAULT_DENSITY = 2.0f;
    private static final float DEFAULT_FRICTION = 0.3f;
    private static final float DEFAULT_RESTITUTION = 0.4f;
    private static final Color DEFAULT_COLOR = Color.BLACK;
    
    public BowlingBall(Vec2 position) {
        super(position);
    }
    
    @Override
    protected float getDefaultRadius() {
        return DEFAULT_RADIUS;
    }
    
    @Override
    protected float getDefaultDensity() {
        return DEFAULT_DENSITY;
    }
    
    @Override
    protected float getDefaultFriction() {
        return DEFAULT_FRICTION;
    }
    
    @Override
    protected float getDefaultRestitution() {
        return DEFAULT_RESTITUTION;
    }
    
    @Override
    protected Color getDefaultColor() {
        return DEFAULT_COLOR;
    }
    
    @Override
    protected String getSoundPrefix() {
        return "bowling";
    }
}
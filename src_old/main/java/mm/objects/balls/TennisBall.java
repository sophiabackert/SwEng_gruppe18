package mm.objects.balls;

import javafx.scene.paint.Color;
import org.jbox2d.common.Vec2;

/**
 * Implementiert einen Tennisball mit spezifischen Eigenschaften.
 */
public class TennisBall extends Ball {
    
    private static final float DEFAULT_RADIUS = 0.3f;
    private static final float DEFAULT_DENSITY = 0.5f;
    private static final float DEFAULT_FRICTION = 0.2f;
    private static final float DEFAULT_RESTITUTION = 0.8f;
    private static final Color DEFAULT_COLOR = Color.YELLOW;
    
    public TennisBall(Vec2 position) {
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
        return "tennis";
    }
}
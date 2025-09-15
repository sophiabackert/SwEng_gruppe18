package mm.model;

/**
 * Basisklasse für alle Spielobjekte.
 */
public abstract class GameObject {
    protected float x;
    protected float y;
    protected float density;
    protected float friction;
    protected float restitution;

    public GameObject(float x, float y) {
        this.x = x;
        this.y = y;
        this.density = 1.0f;      // Standardwert
        this.friction = 0.3f;     // Standardwert
        this.restitution = 0.7f;  // Standardwert
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getDensity() {
        return density;
    }

    public void setDensity(float density) {
        this.density = density;
    }

    public float getFriction() {
        return friction;
    }

    public void setFriction(float friction) {
        this.friction = friction;
    }

    public float getRestitution() {
        return restitution;
    }

    public void setRestitution(float restitution) {
        this.restitution = restitution;
    }
} 
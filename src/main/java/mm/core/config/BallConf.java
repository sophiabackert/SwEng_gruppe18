package mm.core.config;

import java.util.Objects;

/**
 * Basisklasse für alle runden Objekte (z. B. Tennisball, Bowlingball, Balloon)
 */
public abstract class BallConf extends ObjectConf {

    protected final float radius;
    protected final float density;
    protected final float friction;
    protected final float restitution;
    protected final String skinId;

    protected BallConf(float x, float y, float angle, boolean staticFlag,
                       float radius, float density, float friction,
                       float restitution, String skinId) {
        super(x, y, angle, staticFlag);
        this.radius = radius;
        this.density = density;
        this.friction = friction;
        this.restitution = restitution;
        this.skinId = skinId;
        validate();
    }

    public float getRadius()      { return radius; }
    public float getDensity()     { return density; }
    public float getFriction()    { return friction; }
    public float getRestitution() { return restitution; }
    public String getSkinId()     { return skinId; }

    @Override
    public void validate() {
        if (radius <= 0) throw new IllegalArgumentException("radius must be > 0");
        if (density < 0) throw new IllegalArgumentException("density must be ≥ 0");
        if (friction < 0) throw new IllegalArgumentException("friction must be ≥ 0");
        if (restitution < 0 || restitution > 1) throw new IllegalArgumentException("restitution must be in [0,1]");
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        if (!(o instanceof BallConf)) return false;
        BallConf that = (BallConf) o;
        return Float.compare(radius, that.radius) == 0 &&
               Float.compare(density, that.density) == 0 &&
               Float.compare(friction, that.friction) == 0 &&
               Float.compare(restitution, that.restitution) == 0 &&
               skinId.equals(that.skinId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), radius, density, friction, restitution, skinId);
    }
}
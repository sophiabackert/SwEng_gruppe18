package mm.core.config;

import java.util.Objects;

/**
 * Abstrakte Klasse für alle rechteckigen Objekte (Planke, Domino, Cratebox …)
 */
public abstract class BoxConf extends ObjectConf {

    protected final float width;
    protected final float height;
    protected final float density;
    protected final float friction;
    protected final float restitution;
    protected final String skinId;

    protected BoxConf(float x, float y, float angle, boolean staticFlag,
                      float width, float height,
                      float density, float friction, float restitution,
                      String skinId) {
        super(x, y, angle, staticFlag);
        this.width = width;
        this.height = height;
        this.density = density;
        this.friction = friction;
        this.restitution = restitution;
        this.skinId = skinId;
        validate();
    }

    public float getWidth()       { return width; }
    public float getHeight()      { return height; }
    public float getDensity()     { return density; }
    public float getFriction()    { return friction; }
    public float getRestitution() { return restitution; }
    public String getSkinId()     { return skinId; }

    @Override
    public void validate() {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("width and height must be > 0");
        if (density < 0) throw new IllegalArgumentException("density must be ≥ 0");
        if (friction < 0) throw new IllegalArgumentException("friction must be ≥ 0");
        if (restitution < 0 || restitution > 1) throw new IllegalArgumentException("restitution must be in [0,1]");
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        if (!(o instanceof BoxConf)) return false;
        BoxConf that = (BoxConf) o;
        return Float.compare(that.width, width) == 0 &&
               Float.compare(that.height, height) == 0 &&
               Float.compare(that.density, density) == 0 &&
               Float.compare(that.friction, friction) == 0 &&
               Float.compare(that.restitution, restitution) == 0 &&
               skinId.equals(that.skinId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), width, height, density, friction, restitution, skinId);
    }
}
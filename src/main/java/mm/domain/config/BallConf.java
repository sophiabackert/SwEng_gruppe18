package mm.domain.config;

import java.util.Objects;

/**
 * Abstrakte Basisklasse für die Konfiguration runder Spielobjekte wie Tennisball, Bowlingball oder Balloon.
 * <p>
 * Enthält gemeinsame Eigenschaften wie Radius, Dichte, Reibung, Rückprallverhalten und Skin-ID.
 * Diese Klasse wird von konkreten Ball-Konfigurationsklassen erweitert und dient als Grundlage
 * für die physikalischen und grafischen Eigenschaften der Objekte im Spiel.
 * </p>
 *
 * <ul>
 *   <li><b>radius</b>: Der Radius des Balls in Spielfeld-Einheiten.</li>
 *   <li><b>density</b>: Die Dichte des Balls für die Physik-Engine.</li>
 *   <li><b>friction</b>: Die Reibung des Balls auf Oberflächen.</li>
 *   <li><b>restitution</b>: Das Rückprallverhalten (0 = kein Rückprall, 1 = perfekter Rückprall).</li>
 *   <li><b>skinId</b>: Die ID für das grafische Aussehen (Textur/Skin).</li>
 * </ul>
 *
 * @author Dein Name
 */
public abstract class BallConf extends ObjectConf {

    /** Radius des Balls in Spielfeld-Einheiten */
    protected final float radius;
    /** Dichte des Balls für die Physik-Engine */
    protected final float density;
    /** Reibungskoeffizient des Balls */
    protected final float friction;
    /** Rückprallkoeffizient (0 = kein Rückprall, 1 = perfekter Rückprall) */
    protected final float restitution;
    /** Skin-ID für das grafische Aussehen */
    protected final String skinId;

    /**
     * Konstruktor für Ball-Konfigurationen.
     * @param x X-Position des Mittelpunkts
     * @param y Y-Position des Mittelpunkts
     * @param angle Rotationswinkel in Radiant
     * @param staticFlag Ob das Objekt statisch ist
     * @param radius Radius des Balls
     * @param density Dichte für die Physik-Engine
     * @param friction Reibungskoeffizient
     * @param restitution Rückprallkoeffizient
     * @param skinId Skin-ID für das Aussehen
     */
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

    /**
     * @return Radius des Balls
     */
    public float getRadius()      { return radius; }
    /**
     * @return Dichte des Balls
     */
    public float getDensity()     { return density; }
    /**
     * @return Reibungskoeffizient
     */
    public float getFriction()    { return friction; }
    /**
     * @return Rückprallkoeffizient
     */
    public float getRestitution() { return restitution; }
    /**
     * @return Skin-ID für das Aussehen
     */
    public String getSkinId()     { return skinId; }

    /**
     * Validiert die Konfiguration und wirft eine Exception bei ungültigen Werten.
     * @throws IllegalArgumentException wenn Werte ungültig sind
     */
    @Override
    public void validate() {
        if (radius <= 0) throw new IllegalArgumentException("radius must be > 0");
        if (density < 0) throw new IllegalArgumentException("density must be ≥ 0");
        if (friction < 0) throw new IllegalArgumentException("friction must be ≥ 0");
        if (restitution < 0 || restitution > 1) throw new IllegalArgumentException("restitution must be in [0,1]");
    }

    /**
     * Vergleicht diese Ball-Konfiguration mit einer anderen auf Gleichheit.
     * @param o Vergleichsobjekt
     * @return true, wenn alle Eigenschaften gleich sind
     */
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

    /**
     * Berechnet den Hashcode für diese Ball-Konfiguration.
     * @return Hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), radius, density, friction, restitution, skinId);
    }
}
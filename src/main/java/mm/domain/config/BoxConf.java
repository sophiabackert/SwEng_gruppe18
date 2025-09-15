package mm.domain.config;

import java.util.Objects;

/**
 * Abstrakte Basisklasse für die Konfiguration rechteckiger Spielobjekte wie Kiste, Planke, Domino, etc.
 * <p>
 * Enthält gemeinsame Eigenschaften wie Breite, Höhe, Dichte, Reibung, Rückprallverhalten und Skin-ID.
 * Diese Klasse wird von konkreten Box-Konfigurationsklassen erweitert und dient als Grundlage
 * für die physikalischen und grafischen Eigenschaften der Objekte im Spiel.
 * </p>
 *
 * <ul>
 *   <li><b>width</b>: Die Breite des Objekts in Spielfeld-Einheiten.</li>
 *   <li><b>height</b>: Die Höhe des Objekts in Spielfeld-Einheiten.</li>
 *   <li><b>density</b>: Die Dichte für die Physik-Engine.</li>
 *   <li><b>friction</b>: Die Reibung auf Oberflächen.</li>
 *   <li><b>restitution</b>: Das Rückprallverhalten (0 = kein Rückprall, 1 = perfekter Rückprall).</li>
 *   <li><b>skinId</b>: Die ID für das grafische Aussehen (Textur/Skin).</li>
 * </ul>
 */
public abstract class BoxConf extends ObjectConf {

    /** Breite des Objekts in Spielfeld-Einheiten */
    protected final float width;
    /** Höhe des Objekts in Spielfeld-Einheiten */
    protected final float height;
    /** Dichte für die Physik-Engine */
    protected final float density;
    /** Reibungskoeffizient */
    protected final float friction;
    /** Rückprallkoeffizient (0 = kein Rückprall, 1 = perfekter Rückprall) */
    protected final float restitution;
    /** Skin-ID für das grafische Aussehen */
    protected final String skinId;

    /**
     * Konstruktor für Box-Konfigurationen.
     * @param x X-Position des Mittelpunkts
     * @param y Y-Position des Mittelpunkts
     * @param angle Rotationswinkel in Radiant
     * @param staticFlag Ob das Objekt statisch ist
     * @param width Breite
     * @param height Höhe
     * @param density Dichte
     * @param friction Reibung
     * @param restitution Rückprall
     * @param skinId Skin-ID
     */
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

    /**
     * @return Breite des Objekts
     */
    public float getWidth()       { return width; }
    /**
     * @return Höhe des Objekts
     */
    public float getHeight()      { return height; }
    /**
     * @return Dichte
     */
    public float getDensity()     { return density; }
    /**
     * @return Reibung
     */
    public float getFriction()    { return friction; }
    /**
     * @return Rückprallkoeffizient
     */
    public float getRestitution() { return restitution; }
    /**
     * @return Skin-ID
     */
    public String getSkinId()     { return skinId; }

    /**
     * Validiert die Konfiguration und wirft eine Exception bei ungültigen Werten.
     * @throws IllegalArgumentException wenn Werte ungültig sind
     */
    @Override
    public void validate() {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("width and height must be > 0");
        if (density < 0) throw new IllegalArgumentException("density must be ≥ 0");
        if (friction < 0) throw new IllegalArgumentException("friction must be ≥ 0");
        if (restitution < 0 || restitution > 1) throw new IllegalArgumentException("restitution must be in [0,1]");
    }

    /**
     * Vergleicht diese Box-Konfiguration mit einer anderen auf Gleichheit.
     * @param o Vergleichsobjekt
     * @return true, wenn alle Eigenschaften gleich sind
     */
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

    /**
     * Berechnet den Hashcode für diese Box-Konfiguration.
     * @return Hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), width, height, density, friction, restitution, skinId);
    }
}
package mm.domain.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Konfigurationsklasse für einen Eimer (Bucket) im Spiel.
 * <p>
 * Definiert die physikalischen und grafischen Eigenschaften eines Eimers,
 * insbesondere Maße, Wandstärke und Wandwinkel. Wird für die Serialisierung
 * und Erzeugung von Eimer-Objekten verwendet.
 * </p>
 */
public final class BucketConf extends ObjectConf {

    /** Breite des Eimers in Spielfeld-Einheiten */
    private final float width;
    /** Höhe des Eimers in Spielfeld-Einheiten */
    private final float height;
    /** Wandstärke des Eimers */
    private final float thickness;
    /** Winkel der Eimerwände (in Radiant) */
    private final float wallAngle;

    /**
     * Erstellt eine neue Eimer-Konfiguration mit Standardwerten.
     * @param x X-Position des Mittelpunkts
     * @param y Y-Position des Mittelpunkts
     * @param angle Rotationswinkel in Radiant
     * @param staticFlag Ob das Objekt statisch ist
     */
    public BucketConf(
            float x, float y, float angle, boolean staticFlag) {
        this(x, y, angle, staticFlag,
             0.6f,
             0.8f,
             0.05f,
             (float)(85 * Math.PI / 180));
    }

    /**
     * Konstruktor für die Deserialisierung (Jackson).
     * @param x X-Position
     * @param y Y-Position
     * @param angle Rotationswinkel
     * @param staticFlag Ob statisch
     * @param skinId Skin-ID (wird ignoriert)
     */
    @JsonCreator
    public BucketConf(
            @JsonProperty("x")          float x,
            @JsonProperty("y")          float y,
            @JsonProperty("angle")      float angle,
            @JsonProperty("static")     boolean staticFlag,
            @JsonProperty("skinId")     String skinId) {
        this(x, y, angle, staticFlag);
    }

    /**
     * Vollparametrisierter Konstruktor für Eimer.
     * @param x X-Position
     * @param y Y-Position
     * @param angle Rotationswinkel
     * @param staticFlag Ob statisch
     * @param width Breite
     * @param height Höhe
     * @param thickness Wandstärke
     * @param wallAngle Wandwinkel
     */
    public BucketConf(
            float x, float y, float angle, boolean staticFlag,
            float width, float height, float thickness, float wallAngle) {
        super(x, y, angle, staticFlag);
        this.width = width;
        this.height = height;
        this.thickness = thickness;
        this.wallAngle = wallAngle;
        validate();
    }

    /**
     * @return Breite des Eimers
     */
    public float getWidth()      { return width; }
    /**
     * @return Höhe des Eimers
     */
    public float getHeight()     { return height; }
    /**
     * @return Wandstärke
     */
    public float getThickness()  { return thickness; }
    /**
     * @return Wandwinkel (Radiant)
     */
    public float getWallAngle()  { return wallAngle; }
    
    /**
     * @return Skin-ID für das Aussehen
     */
    @Override
    public String getSkinId()    { return "bucket"; }

    /**
     * Validiert die Konfiguration und wirft eine Exception bei ungültigen Werten.
     * @throws IllegalArgumentException wenn Werte ungültig sind
     */
    @Override
    public void validate() {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be > 0");
        }
        if (thickness <= 0) {
            throw new IllegalArgumentException("thickness must be > 0");
        }
        if (wallAngle < 0 || wallAngle > Math.PI / 2) {
            throw new IllegalArgumentException("wallAngle must be between 0 and π/2");
        }
    }

    /**
     * Vergleicht diese Eimer-Konfiguration mit einer anderen auf Gleichheit.
     * @param o Vergleichsobjekt
     * @return true, wenn alle Eigenschaften gleich sind
     */
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        if (!(o instanceof BucketConf)) return false;
        BucketConf that = (BucketConf) o;
        return Float.compare(width, that.width) == 0 &&
               Float.compare(height, that.height) == 0 &&
               Float.compare(thickness, that.thickness) == 0 &&
               Float.compare(wallAngle, that.wallAngle) == 0;
    }

    /**
     * Berechnet den Hashcode für diese Eimer-Konfiguration.
     * @return Hashcode
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), width, height, thickness, wallAngle);
    }
}
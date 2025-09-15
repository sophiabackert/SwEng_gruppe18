package mm.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Ein Bucket besteht aus drei schmalen Boxen (Boden + schräge Seiten).
 * Diese Klasse speichert nur die Position + Gesamtmaße.
 */
public final class BucketConf extends ObjectConf {

    private final float width;      // Breite des Buckets
    private final float height;     // Höhe bis zum oberen Rand
    private final float thickness;  // Dicke der Linien
    private final float wallAngle;  // Seitenwinkel (rad, z. B. 5° ≈ 0.087)

    // Konstruktor für Code-Verwendung (ohne skinId)
    public BucketConf(
            float x, float y, float angle, boolean staticFlag) {
        this(x, y, angle, staticFlag,
             0.6f,                              // width: 60cm Breite
             0.8f,                              // height: 80cm Höhe  
             0.05f,                             // thickness: 5cm dicke Wände
             (float)(85 * Math.PI / 180));      // wallAngle: 95° nach außen
    }

    // JSON-Konstruktor (mit skinId für Deserialisierung)
    @JsonCreator
    public BucketConf(
            @JsonProperty("x")          float x,
            @JsonProperty("y")          float y,
            @JsonProperty("angle")      float angle,
            @JsonProperty("static")     boolean staticFlag,
            @JsonProperty("skinId")     String skinId) {
        this(x, y, angle, staticFlag);
        // skinId wird ignoriert, da getSkinId() bereits "bucket" zurückgibt
    }

    // Vollständiger Konstruktor für erweiterte Konfiguration
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

    public float getWidth()      { return width; }
    public float getHeight()     { return height; }
    public float getThickness()  { return thickness; }
    public float getWallAngle()  { return wallAngle; }
    
    @Override
    public String getSkinId()    { return "bucket"; }

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

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), width, height, thickness, wallAngle);
    }
}
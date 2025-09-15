package mm.domain.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Konfigurationsklasse für eine Restriktionszone (RestrictionZone) im Spiel.
 * <p>
 * Definiert die physikalischen und grafischen Eigenschaften einer Restriktionszone,
 * insbesondere Breite und Höhe. Wird für die Serialisierung und Erzeugung
 * von Restriktionszonen-Objekten verwendet.
 * </p>
 */
public class RestrictionZoneConf extends ObjectConf {
    
    /** Breite der Restriktionszone in Spielfeld-Einheiten */
    protected final float width;
    /** Höhe der Restriktionszone in Spielfeld-Einheiten */
    protected final float height;
    
    /**
     * Vollparametrisierter Konstruktor für die Restriktionszone.
     * @param x X-Position
     * @param y Y-Position
     * @param angle Rotationswinkel
     * @param staticFlag Wird ignoriert (immer statisch)
     * @param width Breite
     * @param height Höhe
     * @param skinId Skin-ID (wird ignoriert)
     */
    public RestrictionZoneConf(float x, float y, float angle, boolean staticFlag, 
                        float width, float height, String skinId) {
        super(x, y, angle, true);
        this.width = width;
        this.height = height;
        validate();
    }
    
    /**
     * Konstruktor für die Restriktionszone mit Standardmaßen.
     * @param x X-Position
     * @param y Y-Position
     * @param angle Rotationswinkel
     * @param staticFlagIgnored Wird ignoriert
     */
    public RestrictionZoneConf(float x, float y, float angle, boolean staticFlagIgnored) {
        this(x, y, angle, true, 1.0f, 0.6f, "restrictionzone");
    }
    
    /**
     * Konstruktor für die Deserialisierung (Jackson).
     * @param x X-Position
     * @param y Y-Position
     * @param angle Rotationswinkel
     * @param staticFlagIgnored Wird ignoriert
     * @param skinId Skin-ID (wird ignoriert)
     */
    @JsonCreator
    public RestrictionZoneConf(
            @JsonProperty("x")          float x,
            @JsonProperty("y")          float y,
            @JsonProperty("angle")      float angle,
            @JsonProperty("static")     boolean staticFlagIgnored,
            @JsonProperty("skinId")     String skinId) {
        this(x, y, angle, true);
    }
    
    /**
     * Erstellt eine Standard-Restriktionszone (Position 0,0,0, statisch).
     */
    public RestrictionZoneConf() {
        this(0, 0, 0, true);
    }
    
    /**
     * @return Breite der Restriktionszone
     */
    public float getWidth() { return width; }
    /**
     * @return Höhe der Restriktionszone
     */
    public float getHeight() { return height; }
    
    /**
     * @return Skin-ID für das Aussehen
     */
    @Override
    public String getSkinId() { return "restrictionzone"; }
    
    /**
     * Validiert die Konfiguration und wirft eine Exception bei ungültigen Werten.
     * @throws IllegalArgumentException wenn Werte ungültig sind
     */
    @Override
    public void validate() {
        if (width <= 0) throw new IllegalArgumentException("width must be > 0");
        if (height <= 0) throw new IllegalArgumentException("height must be > 0");
    }
    
    /**
     * Vergleicht diese Restriktionszonen-Konfiguration mit einer anderen auf Gleichheit.
     * @param o Vergleichsobjekt
     * @return true, wenn alle Eigenschaften gleich sind
     */
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        if (!(o instanceof RestrictionZoneConf)) return false;
        RestrictionZoneConf that = (RestrictionZoneConf) o;
        return Float.compare(width, that.width) == 0 &&
               Float.compare(height, that.height) == 0;
    }
    
    /**
     * Berechnet den Hashcode für diese Restriktionszonen-Konfiguration.
     * @return Hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), width, height);
    }
} 
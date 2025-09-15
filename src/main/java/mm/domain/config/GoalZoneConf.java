package mm.domain.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Konfigurationsklasse für die Zielzone (GoalZone) im Spiel.
 * <p>
 * Definiert die physikalischen und grafischen Eigenschaften der Zielzone,
 * insbesondere Breite und Höhe. Wird für die Serialisierung und Erzeugung
 * von Zielzonen-Objekten verwendet.
 * </p>
 */
public class GoalZoneConf extends ObjectConf {
    
    /** Breite der Zielzone in Spielfeld-Einheiten */
    protected final float width;
    /** Höhe der Zielzone in Spielfeld-Einheiten */
    protected final float height;
    
    /**
     * Vollparametrisierter Konstruktor für die Zielzone.
     * @param x X-Position
     * @param y Y-Position
     * @param angle Rotationswinkel
     * @param staticFlag Wird ignoriert (Zielzone ist immer statisch)
     * @param width Breite
     * @param height Höhe
     * @param skinId Skin-ID (wird ignoriert)
     */
    public GoalZoneConf(float x, float y, float angle, boolean staticFlag, 
                        float width, float height, String skinId) {
        super(x, y, angle, true);
        this.width = width;
        this.height = height;
        validate();
    }
    
    /**
     * Konstruktor für die Zielzone mit Standardmaßen.
     * @param x X-Position
     * @param y Y-Position
     * @param angle Rotationswinkel
     * @param staticFlag Wird ignoriert
     */
    public GoalZoneConf(float x, float y, float angle, boolean staticFlagIgnored) {
        this(x, y, angle, true, 1.0f, 0.6f, "goalzone");
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
    public GoalZoneConf(
            @JsonProperty("x")          float x,
            @JsonProperty("y")          float y,
            @JsonProperty("angle")      float angle,
            @JsonProperty("static")     boolean staticFlagIgnored,
            @JsonProperty("skinId")     String skinId) {
        this(x, y, angle, true);
    }
    
    /**
     * Erstellt eine Standard-Zielzone (Position 0,0,0, statisch).
     */
    public GoalZoneConf() {
        this(0, 0, 0, true);
    }
    
    /**
     * @return Breite der Zielzone
     */
    public float getWidth() { return width; }
    /**
     * @return Höhe der Zielzone
     */
    public float getHeight() { return height; }
    
    /**
     * @return Skin-ID für das Aussehen
     */
    @Override
    public String getSkinId() { return "goalzone"; }
    
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
     * Vergleicht diese Zielzonen-Konfiguration mit einer anderen auf Gleichheit.
     * @param o Vergleichsobjekt
     * @return true, wenn alle Eigenschaften gleich sind
     */
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        if (!(o instanceof GoalZoneConf)) return false;
        GoalZoneConf that = (GoalZoneConf) o;
        return Float.compare(width, that.width) == 0 &&
               Float.compare(height, that.height) == 0;
    }
    
    /**
     * Berechnet den Hashcode für diese Zielzonen-Konfiguration.
     * @return Hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), width, height);
    }
} 
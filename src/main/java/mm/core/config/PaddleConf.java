package mm.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Konfigurationsklasse für Paddle-Objekte.
 * Ein Paddle ist an einem Zahnrad befestigt und rotiert mit diesem.
 */
public final class PaddleConf extends ObjectConf {
    
    private final String attachedGearId;  // ID des Zahnrads, an dem das Paddle befestigt ist
    private final float relativeAngle;    // Relative Rotationsposition zum Zahnrad
    private final float length;           // Länge des Paddles
    private final float width;            // Breite des Paddles
    
    /**
     * Standard-Konstruktor für neue Paddles
     */
    public PaddleConf(float x, float y, float angle, String attachedGearId) {
        this(x, y, angle, true, attachedGearId, 0.0f, 1.0f, 0.2f);
    }
    
    /**
     * Vollständiger Konstruktor (für JSON-Deserialisierung)
     */
    @JsonCreator
    public PaddleConf(
            @JsonProperty("x") float x,
            @JsonProperty("y") float y,
            @JsonProperty("angle") float angle,
            @JsonProperty("static") boolean staticFlag,
            @JsonProperty("attachedGearId") String attachedGearId,
            @JsonProperty("relativeAngle") float relativeAngle,
            @JsonProperty("length") float length,
            @JsonProperty("width") float width) {
        super(x, y, angle, staticFlag);
        this.attachedGearId = attachedGearId != null ? attachedGearId : "";
        this.relativeAngle = relativeAngle;
        this.length = length > 0 ? length : 1.0f;
        this.width = width > 0 ? width : 0.2f;
    }
    
    // Getter-Methoden
    public String getAttachedGearId() {
        return attachedGearId;
    }
    
    public float getRelativeAngle() {
        return relativeAngle;
    }
    
    public float getLength() {
        return length;
    }
    
    public float getWidth() {
        return width;
    }
    
    @Override
    public String getSkinId() {
        return "paddle";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (!(obj instanceof PaddleConf)) return false;
        
        PaddleConf other = (PaddleConf) obj;
        return Float.compare(other.relativeAngle, relativeAngle) == 0 &&
               Float.compare(other.length, length) == 0 &&
               Float.compare(other.width, width) == 0 &&
               attachedGearId.equals(other.attachedGearId);
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + attachedGearId.hashCode();
        result = 31 * result + Float.hashCode(relativeAngle);
        result = 31 * result + Float.hashCode(length);
        result = 31 * result + Float.hashCode(width);
        return result;
    }
    
    @Override
    public String toString() {
        return String.format("PaddleConf{attachedGearId='%s', x=%.2f, y=%.2f, angle=%.2f, length=%.2f, width=%.2f}", 
                           attachedGearId, getX(), getY(), getAngle(), length, width);
    }
} 
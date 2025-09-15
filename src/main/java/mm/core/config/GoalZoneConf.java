package mm.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Konfiguration für die Goalzone - eine rechteckige Zone, durch die sich Objekte bewegen können.
 * Die Goalzone ist immer statisch und kann nicht bewegt werden.
 */
public class GoalZoneConf extends ObjectConf {
    
    protected final float width;
    protected final float height;
    
    public GoalZoneConf(float x, float y, float angle, boolean staticFlag, 
                        float width, float height, String skinId) {
        super(x, y, angle, true);  // immer statisch!
        this.width = width;
        this.height = height;
        validate();
    }
    
    public GoalZoneConf(float x, float y, float angle, boolean staticFlagIgnored) {
        this(x, y, angle, true, 1.0f, 0.6f, "goalzone"); // staticFlag wird ignoriert
    }
    
    // JSON-Konstruktor für Jackson-Deserialisierung
    @JsonCreator
    public GoalZoneConf(
            @JsonProperty("x")          float x,
            @JsonProperty("y")          float y,
            @JsonProperty("angle")      float angle,
            @JsonProperty("static")     boolean staticFlagIgnored,  // wird ignoriert
            @JsonProperty("skinId")     String skinId) {
        this(x, y, angle, true); // verwendet Standardgrößen, immer statisch
    }
    
    public GoalZoneConf() {
        this(0, 0, 0, true);
    }
    
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    
    @Override
    public String getSkinId() { return "goalzone"; }
    
    @Override
    public void validate() {
        if (width <= 0) throw new IllegalArgumentException("width must be > 0");
        if (height <= 0) throw new IllegalArgumentException("height must be > 0");
    }
    
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        if (!(o instanceof GoalZoneConf)) return false;
        GoalZoneConf that = (GoalZoneConf) o;
        return Float.compare(width, that.width) == 0 &&
               Float.compare(height, that.height) == 0;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), width, height);
    }
} 
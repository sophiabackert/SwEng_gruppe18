package mm.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Konfiguration für einen Antriebsstrang zwischen zwei Zahnrädern.
 */
public final class DriveChainConf extends ObjectConf {
    
    private final String gearAId;  // ID des ersten Zahnrads
    private final String gearBId;  // ID des zweiten Zahnrads
    private final float thickness; // Dicke der Verbindungslinie
    
    @JsonCreator
    public DriveChainConf(
            @JsonProperty("x") float x,
            @JsonProperty("y") float y,
            @JsonProperty("angle") float angle,
            @JsonProperty("gearAId") String gearAId,
            @JsonProperty("gearBId") String gearBId,
            @JsonProperty("thickness") float thickness) {
        super(x, y, angle, true); // Antriebsstränge sind immer statisch
        this.gearAId = gearAId;
        this.gearBId = gearBId;
        this.thickness = thickness;
    }
    
    // Standard-Konstruktor
    public DriveChainConf(float x, float y, float angle, String gearAId, String gearBId) {
        this(x, y, angle, gearAId, gearBId, 0.05f); // Standard-Dicke: 5cm
    }
    
    // Getters
    public String getGearAId() { return gearAId; }
    public String getGearBId() { return gearBId; }
    public float getThickness() { return thickness; }
    
    @Override
    public String getSkinId() { return "drivechain"; }
    
    @Override
    public void validate() {
        if (gearAId == null || gearAId.isEmpty()) {
            throw new IllegalArgumentException("gearAId darf nicht null oder leer sein");
        }
        if (gearBId == null || gearBId.isEmpty()) {
            throw new IllegalArgumentException("gearBId darf nicht null oder leer sein");
        }
        if (gearAId.equals(gearBId)) {
            throw new IllegalArgumentException("gearAId und gearBId müssen unterschiedlich sein");
        }
        if (thickness <= 0) {
            throw new IllegalArgumentException("thickness muss größer als 0 sein");
        }
    }
} 
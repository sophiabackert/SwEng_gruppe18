package mm.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;

/**
 * Einfache Zahnrad-Konfiguration mit zwei konzentrischen Kreisen.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GearConf extends ObjectConf {
    
    private final GearSize gearSize;
    private final String skinId;
    private List<String> connectedGearIds;  // IDs der verbundenen Zahnräder (nicht final für Jackson)
    
    public enum GearSize {
        SMALL("smallgear", 0.164f, 0.205f),  // 55% kleiner: 0.365*0.45, 0.455*0.45
        LARGE("largegear", 0.290f, 0.338f);  // 55% kleiner: 0.645*0.45, 0.75*0.45
        
        private final String skinId;
        private final float innerRadius;
        private final float outerRadius;
        
        GearSize(String skinId, float innerRadius, float outerRadius) {
            this.skinId = skinId;
            this.innerRadius = innerRadius;
            this.outerRadius = outerRadius;
        }
        
        public String getSkinId() { return skinId; }
        public float getInnerRadius() { return innerRadius; }
        public float getOuterRadius() { return outerRadius; }
    }
    
    // Standard-Konstruktor für kleine Zahnräder
    public GearConf(float x, float y, float angle, boolean staticFlag) {
        this(x, y, angle, staticFlag, GearSize.SMALL);
    }
    
    // Konstruktor mit Größenangabe
    public GearConf(float x, float y, float angle, boolean staticFlag, GearSize size) {
        super(x, y, angle, true); // Zahnräder sind immer statisch
        this.gearSize = size;
        this.skinId = size.getSkinId();
        this.connectedGearIds = new ArrayList<>();
    }
    
    // JSON-Konstruktor
    @JsonCreator
    public GearConf(
            @JsonProperty("x") float x,
            @JsonProperty("y") float y,
            @JsonProperty("angle") float angle,
            @JsonProperty("static") boolean staticFlag,
            @JsonProperty("skinId") String skinId,
            @JsonProperty("connectedGearIds") List<String> connectedGearIds) {
        super(x, y, angle, true); // Zahnräder sind immer statisch
        this.gearSize = skinId.equals("largegear") ? GearSize.LARGE : GearSize.SMALL;
        this.skinId = this.gearSize.getSkinId();
        this.connectedGearIds = connectedGearIds != null ? new ArrayList<>(connectedGearIds) : new ArrayList<>();
    }
    
    // Getters
    public float getInnerRadius() { return gearSize.getInnerRadius(); }
    public float getOuterRadius() { return gearSize.getOuterRadius(); }
    public GearSize getGearSize() { return gearSize; }
    public List<String> getConnectedGearIds() { return connectedGearIds; }
    
    // Methode zum Hinzufügen einer Verbindung
    public void addConnection(String gearId) {
        if (!connectedGearIds.contains(gearId)) {
            connectedGearIds.add(gearId);
        }
    }
    
    // Methode zum Entfernen einer Verbindung
    public void removeConnection(String gearId) {
        connectedGearIds.remove(gearId);
    }
    
    @Override
    public String getSkinId() { return skinId; }
    
    @Override
    public void validate() {
        // Validierung nicht nötig, da alle Werte fest definiert sind
    }
    
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        if (!(o instanceof GearConf)) return false;
        GearConf that = (GearConf) o;
        return gearSize == that.gearSize &&
               Objects.equals(connectedGearIds, that.connectedGearIds);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), gearSize, connectedGearIds);
    }
} 
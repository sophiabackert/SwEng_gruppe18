package mm.domain.config;

import com.fasterxml.jackson.annotation.*;
import java.util.Objects;

/**
 * Abstrakte Basisklasse für die Konfiguration aller Spielobjekte.
 * <p>
 * Enthält gemeinsame Eigenschaften wie Position, Rotation und Statisch-Flag.
 * Dient als Grundlage für die Serialisierung, Validierung und Vergleichbarkeit
 * aller konkreten Objekt-Konfigurationen im Spiel.
 * </p>
 *
 * Die Klasse ist für die Verwendung mit Jackson annotiert, um die korrekte
 * (De-)Serialisierung von Subtypen anhand der Skin-ID zu ermöglichen.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "skinId",
        visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = TennisballConf.class, name = "tennisball"),
    @JsonSubTypes.Type(value = BalloonConf.class,     name = "balloon"),
    @JsonSubTypes.Type(value = BowlingballConf.class,name = "bowlingball"),
    @JsonSubTypes.Type(value = BilliardballConf.class,name = "billiardball"),
    @JsonSubTypes.Type(value = LogConf.class,        name = "log"),
    @JsonSubTypes.Type(value = PlankConf.class,      name = "plank"),
    @JsonSubTypes.Type(value = DominoConf.class,     name = "domino"),
    @JsonSubTypes.Type(value = CrateboxConf.class,   name = "cratebox"),
    @JsonSubTypes.Type(value = BucketConf.class,     name = "bucket"),
    @JsonSubTypes.Type(value = GameBallConf.class,   name = "gameball"),
    @JsonSubTypes.Type(value = GoalZoneConf.class,   name = "goalzone"),
    @JsonSubTypes.Type(value = RestrictionZoneConf.class, name = "restrictionzone")
})
public abstract class ObjectConf {

    /** X-Position des Objekts (Spielfeld-Einheiten) */
    protected final float x;
    /** Y-Position des Objekts (Spielfeld-Einheiten) */
    protected final float y;
    /** Rotationswinkel des Objekts (Radiant) */
    protected final float angle;
    /** Gibt an, ob das Objekt statisch ist */
    protected final boolean staticFlag;

    /**
     * Konstruktor für Objekt-Konfigurationen.
     * @param x X-Position
     * @param y Y-Position
     * @param angle Rotationswinkel
     * @param staticFlag Ob das Objekt statisch ist
     */
    protected ObjectConf(float x, float y, float angle, boolean staticFlag) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.staticFlag = staticFlag;
    }

    /**
     * @return X-Position des Objekts
     */
    public float getX()         { return x; }
    /**
     * @return Y-Position des Objekts
     */
    public float getY()         { return y; }
    /**
     * @return Rotationswinkel (Radiant)
     */
    public float getAngle()     { return angle; }
    
    /**
     * @return true, wenn das Objekt statisch ist
     */
    @JsonProperty("static")
    public boolean isStatic()   { return staticFlag; }

    /**
     * @return Skin-ID für die (De-)Serialisierung und das Aussehen
     */
    public abstract String getSkinId();

    /**
     * Validiert die Konfiguration. Kann von Subklassen überschrieben werden.
     * @throws IllegalArgumentException bei ungültigen Werten
     */
    public void validate() {}

    /**
     * Vergleicht diese Objekt-Konfiguration mit einer anderen auf Gleichheit.
     * @param o Vergleichsobjekt
     * @return true, wenn alle Eigenschaften gleich sind
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof ObjectConf)) return false;
        ObjectConf that = (ObjectConf) o;
        return Float.compare(that.x, x) == 0 &&
               Float.compare(that.y, y) == 0 &&
               Float.compare(that.angle, angle) == 0 &&
               staticFlag == that.staticFlag;
    }

    /**
     * Berechnet den Hashcode für diese Objekt-Konfiguration.
     * @return Hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(x, y, angle, staticFlag);
    }
}
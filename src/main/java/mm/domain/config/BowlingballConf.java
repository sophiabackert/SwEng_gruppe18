package mm.domain.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Konfigurationsklasse für einen Bowlingball im Spiel.
 * <p>
 * Definiert die physikalischen und grafischen Eigenschaften eines Bowlingballs
 * (z. B. Radius, Dichte, Reibung, Rückprall, Skin-ID) durch Vorgabe an die Basisklasse BallConf.
 * Wird für die Serialisierung und Erzeugung von Bowlingball-Objekten verwendet.
 * </p>
 */
public final class BowlingballConf extends BallConf {

    /**
     * Erstellt eine neue Bowlingball-Konfiguration mit festen Eigenschaften.
     * @param x X-Position des Mittelpunkts
     * @param y Y-Position des Mittelpunkts
     * @param angle Rotationswinkel in Radiant
     * @param staticFlag Ob das Objekt statisch ist
     */
    @JsonCreator
    public BowlingballConf(
            @JsonProperty("x")          float x,
            @JsonProperty("y")          float y,
            @JsonProperty("angle")      float angle,
            @JsonProperty("static")     boolean staticFlag) {
        super(x, y, angle, staticFlag,
              0.30f,
              2.0f,
              0.4f,
              0.05f,
              "bowlingball");
    }
}
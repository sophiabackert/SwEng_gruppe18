package mm.domain.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Konfigurationsklasse für einen Tennisball im Spiel.
 * <p>
 * Definiert die physikalischen und grafischen Eigenschaften eines Tennisballs
 * (z. B. Radius, Dichte, Reibung, Rückprall, Skin-ID) durch Vorgabe an die Basisklasse BallConf.
 * Wird für die Serialisierung und Erzeugung von Tennisball-Objekten verwendet.
 * </p>
 */
public final class TennisballConf extends BallConf {

    /**
     * Erstellt eine neue Tennisball-Konfiguration mit festen Eigenschaften.
     * @param x X-Position des Mittelpunkts
     * @param y Y-Position des Mittelpunkts
     * @param angle Rotationswinkel in Radiant
     * @param staticFlag Ob das Objekt statisch ist
     */
    @JsonCreator
    public TennisballConf(
            @JsonProperty("x")          float x,
            @JsonProperty("y")          float y,
            @JsonProperty("angle")      float angle,
            @JsonProperty("static")     boolean staticFlag) {
        super(x, y, angle, staticFlag,
              0.12f,
              0.7f,
              0.2f,
              0.55f,
              "tennisball");
    }
}
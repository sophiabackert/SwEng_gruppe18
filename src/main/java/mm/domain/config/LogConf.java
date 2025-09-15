package mm.domain.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Konfigurationsklasse für einen Baumstamm (Log) im Spiel.
 * <p>
 * Definiert die physikalischen und grafischen Eigenschaften eines Baumstamms
 * (z. B. Radius, Dichte, Reibung, Rückprall, Skin-ID) durch Vorgabe an die Basisklasse BallConf.
 * Wird für die Serialisierung und Erzeugung von Log-Objekten verwendet.
 * </p>
 */
public final class LogConf extends BallConf {

    /**
     * Erstellt eine neue Log-Konfiguration mit festen Eigenschaften.
     * @param x X-Position des Mittelpunkts
     * @param y Y-Position des Mittelpunkts
     * @param angle Rotationswinkel in Radiant
     * @param staticFlag Ob das Objekt statisch ist
     */
    @JsonCreator
    public LogConf(
            @JsonProperty("x")          float x,
            @JsonProperty("y")          float y,
            @JsonProperty("angle")      float angle,
            @JsonProperty("static")     boolean staticFlag) {
        super(x, y, angle, true,
              0.3f,
              1.2f,
              0.8f,
              0.05f,
              "log");
    }
}
package mm.domain.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Konfigurationsklasse für einen Ballon im Spiel.
 * <p>
 * Definiert die physikalischen und grafischen Eigenschaften eines Ballons
 * (z. B. Radius, Dichte, Reibung, Rückprall, Skin-ID) durch Vorgabe an die Basisklasse BallConf.
 * Wird für die Serialisierung und Erzeugung von Ballon-Objekten verwendet.
 * </p>
 */
public class BalloonConf extends BallConf {

    /**
     * Erstellt eine neue Ballon-Konfiguration mit festen Eigenschaften.
     * @param x X-Position des Mittelpunkts
     * @param y Y-Position des Mittelpunkts
     * @param angle Rotationswinkel in Radiant
     * @param staticFlag Ob das Objekt statisch ist
     */
    @JsonCreator
    public BalloonConf(
            @JsonProperty("x")          float x,
            @JsonProperty("y")          float y,
            @JsonProperty("angle")      float angle,
            @JsonProperty("static")     boolean staticFlag) {
        super(x, y, angle, staticFlag,
              0.35f,
              0.3f,  // Reduzierte Dichte für langsameren Aufstieg
              0.01f,
              0.90f,
              "balloon");
    }
}
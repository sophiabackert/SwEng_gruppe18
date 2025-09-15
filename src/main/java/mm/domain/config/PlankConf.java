package mm.domain.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Konfigurationsklasse für eine Planke (Plank) im Spiel.
 * <p>
 * Definiert die physikalischen und grafischen Eigenschaften einer Planke
 * (z. B. Maße, Dichte, Reibung, Rückprall, Skin-ID) durch Vorgabe an die Basisklasse BoxConf.
 * Wird für die Serialisierung und Erzeugung von Planken-Objekten verwendet.
 * </p>
 */
public final class PlankConf extends BoxConf {

    /**
     * Erstellt eine neue Planken-Konfiguration mit festen Eigenschaften.
     * @param x X-Position des Mittelpunkts
     * @param y Y-Position des Mittelpunkts
     * @param angle Rotationswinkel in Radiant
     * @param staticFlagIgnored Wird ignoriert (Planken sind immer statisch)
     */
    @JsonCreator
    public PlankConf(
            @JsonProperty("x")          float x,
            @JsonProperty("y")          float y,
            @JsonProperty("angle")      float angle,
            @JsonProperty("static")     boolean staticFlagIgnored) {
        super(x, y, angle, true,
              2.0f, 0.327f,
              0.8f, 0.4f, 0.1f,
              "plank");
    }
}
package mm.domain.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Konfigurationsklasse für einen Dominostein im Spiel.
 * <p>
 * Definiert die physikalischen und grafischen Eigenschaften eines Dominosteins
 * (z. B. Maße, Dichte, Reibung, Rückprall, Skin-ID) durch Vorgabe an die Basisklasse BoxConf.
 * Wird für die Serialisierung und Erzeugung von Domino-Objekten verwendet.
 * </p>
 */
public final class DominoConf extends BoxConf {

    /**
     * Erstellt eine neue Domino-Konfiguration mit festen Eigenschaften.
     * @param x X-Position des Mittelpunkts
     * @param y Y-Position des Mittelpunkts
     * @param angle Rotationswinkel in Radiant
     * @param staticFlag Ob das Objekt statisch ist
     */
    @JsonCreator
    public DominoConf(
            @JsonProperty("x")          float x,
            @JsonProperty("y")          float y,
            @JsonProperty("angle")      float angle,
            @JsonProperty("static")     boolean staticFlag) {
        super(x, y, angle, staticFlag,
              0.15f, 0.6f,
              1.0f, 0.3f, 0.2f,
              "domino");
    }
}
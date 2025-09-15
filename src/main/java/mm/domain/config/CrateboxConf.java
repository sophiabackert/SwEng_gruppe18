package mm.domain.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Konfigurationsklasse für eine Kiste (Cratebox) im Spiel.
 * <p>
 * Definiert die physikalischen und grafischen Eigenschaften einer Kiste
 * (z. B. Maße, Dichte, Reibung, Rückprall, Skin-ID) durch Vorgabe an die Basisklasse BoxConf.
 * Wird für die Serialisierung und Erzeugung von Kisten-Objekten verwendet.
 * </p>
 */
public final class CrateboxConf extends BoxConf {

    /**
     * Erstellt eine neue Kisten-Konfiguration mit festen Eigenschaften.
     * @param x X-Position des Mittelpunkts
     * @param y Y-Position des Mittelpunkts
     * @param angle Rotationswinkel in Radiant
     * @param staticFlag Ob das Objekt statisch ist
     */
    @JsonCreator
    public CrateboxConf(
            @JsonProperty("x")          float x,
            @JsonProperty("y")          float y,
            @JsonProperty("angle")      float angle,
            @JsonProperty("static")     boolean staticFlag) {
        super(x, y, angle, staticFlag,
              0.85f, 0.85f,
              0.9f, 0.5f, 0.1f,
              "cratebox");
    }
}
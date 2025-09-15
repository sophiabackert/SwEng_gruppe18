package mm.domain.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Konfigurationsklasse für den Spielball (GameBall) im Spiel.
 * <p>
 * Definiert die physikalischen und grafischen Eigenschaften des Spielballs
 * (z. B. Radius, Dichte, Reibung, Rückprall, Skin-ID) durch Vorgabe an die Basisklasse BallConf.
 * Wird für die Serialisierung und Erzeugung des Spielballs verwendet.
 * </p>
 */
public class GameBallConf extends BallConf {
    
    /**
     * Erstellt eine neue Spielball-Konfiguration mit festen Eigenschaften.
     * @param x X-Position des Mittelpunkts
     * @param y Y-Position des Mittelpunkts
     * @param angle Rotationswinkel in Radiant
     * @param staticFlag Ob das Objekt statisch ist
     */
    @JsonCreator
    public GameBallConf(
            @JsonProperty("x")          float x,
            @JsonProperty("y")          float y,
            @JsonProperty("angle")      float angle,
            @JsonProperty("static")     boolean staticFlag) {
        super(x, y, angle, staticFlag, 
              0.12f,
              1.0f,
              0.3f,
              0.7f,
              "gameball");
    }
    
    /**
     * Erstellt eine Standard-Spielball-Konfiguration (Position 0,0,0, nicht statisch).
     */
    public GameBallConf() {
        this(0, 0, 0, false);
    }
} 
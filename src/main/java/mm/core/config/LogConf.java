package mm.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class LogConf extends BallConf {

    @JsonCreator
    public LogConf(
            @JsonProperty("x")          float x,
            @JsonProperty("y")          float y,
            @JsonProperty("angle")      float angle,
            @JsonProperty("static")     boolean staticFlag) {
        super(x, y, angle, true,      // immer statisch!
              0.3f,    // Radius ≈ halbe Breite des Stamms
              1.2f,    // mittlere Dichte
              0.8f,    // hohe Reibung (rollt nicht leicht)
              0.05f,   // fast kein Bouncen
              "log");
    }
}
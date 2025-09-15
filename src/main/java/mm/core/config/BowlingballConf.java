package mm.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class BowlingballConf extends BallConf {

    @JsonCreator
    public BowlingballConf(
            @JsonProperty("x")          float x,
            @JsonProperty("y")          float y,
            @JsonProperty("angle")      float angle,
            @JsonProperty("static")     boolean staticFlag) {
        super(x, y, angle, staticFlag,
              0.30f,   // kleiner Radius
              2.0f,    // sehr schwer
              0.4f,    // mittlere Reibung
              0.05f,   // fast kein Bouncen
              "bowlingball");
    }
}
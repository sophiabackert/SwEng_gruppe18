package mm.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class BilliardballConf extends BallConf {

    @JsonCreator
    public BilliardballConf(
            @JsonProperty("x")          float x,
            @JsonProperty("y")          float y,
            @JsonProperty("angle")      float angle,
            @JsonProperty("static")     boolean staticFlag) {
        super(x, y, angle, staticFlag,
              0.1f,  // sehr klein
              1.6f,    // leicht
              0.3f,    // geringfügige Reibung
              0.20f,   // kaum elastisch
              "billiardball");
    }
}
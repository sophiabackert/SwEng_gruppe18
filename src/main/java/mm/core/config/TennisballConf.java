package mm.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Tennisball mit festen Parametern (Skin-ID: "tennisball")
 */
public final class TennisballConf extends BallConf {

    @JsonCreator
    public TennisballConf(
            @JsonProperty("x")          float x,
            @JsonProperty("y")          float y,
            @JsonProperty("angle")      float angle,
            @JsonProperty("static")     boolean staticFlag) {
        super(x, y, angle, staticFlag,
              0.12f,     // radius
              0.7f,      // density
              0.2f,      // friction
              0.55f,     // restitution
              "tennisball"); // skinId
    }
}
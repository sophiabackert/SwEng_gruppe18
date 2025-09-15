package mm.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BalloonConf extends BallConf {

    @JsonCreator
    public BalloonConf(
            @JsonProperty("x")          float x,
            @JsonProperty("y")          float y,
            @JsonProperty("angle")      float angle,
            @JsonProperty("static")     boolean staticFlag) {
        super(x, y, angle, staticFlag,
              0.35f,    // radius (groß)
              0.05f,    // sehr leicht
              0.01f,    // fast keine Reibung
              0.90f,    // extrem elastisch
              "balloon");
    }
}
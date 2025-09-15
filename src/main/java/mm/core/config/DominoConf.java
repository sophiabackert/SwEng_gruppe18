package mm.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class DominoConf extends BoxConf {

    @JsonCreator
    public DominoConf(
            @JsonProperty("x")          float x,
            @JsonProperty("y")          float y,
            @JsonProperty("angle")      float angle,
            @JsonProperty("static")     boolean staticFlag) {
        super(x, y, angle, staticFlag,
              0.15f, 0.6f,                 // width, height
              1.0f, 0.3f, 0.2f,           // density, friction, restitution
              "domino");
    }
}
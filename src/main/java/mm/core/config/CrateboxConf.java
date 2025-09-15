package mm.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class CrateboxConf extends BoxConf {

    @JsonCreator
    public CrateboxConf(
            @JsonProperty("x")          float x,
            @JsonProperty("y")          float y,
            @JsonProperty("angle")      float angle,
            @JsonProperty("static")     boolean staticFlag) {
        super(x, y, angle, staticFlag,
              0.85f, 0.85f,                 // width, height
              0.9f, 0.5f, 0.1f,           // density, friction, restitution
              "cratebox");
    }
}
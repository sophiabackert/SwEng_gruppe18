package mm.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class PlankConf extends BoxConf {

    @JsonCreator
    public PlankConf(
            @JsonProperty("x")          float x,
            @JsonProperty("y")          float y,
            @JsonProperty("angle")      float angle,
            @JsonProperty("static")     boolean staticFlagIgnored) {
        super(x, y, angle, true,           // immer statisch!
              2.0f, 0.327f,                  // width, height
              0.8f, 0.4f, 0.1f,            // density, friction, restitution
              "plank");                   // skinId
    }
}
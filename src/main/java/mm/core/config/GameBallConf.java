package mm.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Konfiguration für den Spielball.
 */
public class GameBallConf extends BallConf {
    
    @JsonCreator
    public GameBallConf(
            @JsonProperty("x")          float x,
            @JsonProperty("y")          float y,
            @JsonProperty("angle")      float angle,
            @JsonProperty("static")     boolean staticFlag) {
        super(x, y, angle, staticFlag, 
              0.12f,   // radius - in Metern, wie andere Bälle
              1.0f,    // density  
              0.3f,    // friction
              0.7f,    // restitution
              "gameball"); // skinId
    }
    
    public GameBallConf() {
        this(0, 0, 0, false);
    }
} 
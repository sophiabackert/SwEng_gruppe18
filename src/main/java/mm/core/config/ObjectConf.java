package mm.core.config;

import com.fasterxml.jackson.annotation.*;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "skinId",
        visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = TennisballConf.class, name = "tennisball"),
    @JsonSubTypes.Type(value = BalloonConf.class,     name = "balloon"),
    @JsonSubTypes.Type(value = BowlingballConf.class,name = "bowlingball"),
    @JsonSubTypes.Type(value = BilliardballConf.class,name = "billiardball"),
    @JsonSubTypes.Type(value = LogConf.class,        name = "log"),
    @JsonSubTypes.Type(value = PlankConf.class,      name = "plank"),
    @JsonSubTypes.Type(value = DominoConf.class,     name = "domino"),
    @JsonSubTypes.Type(value = CrateboxConf.class,   name = "cratebox"),
    @JsonSubTypes.Type(value = BucketConf.class,     name = "bucket"),
    @JsonSubTypes.Type(value = GameBallConf.class,   name = "gameball"),
    @JsonSubTypes.Type(value = GoalZoneConf.class,   name = "goalzone"),
    @JsonSubTypes.Type(value = GearConf.class,       name = "smallgear"),
    @JsonSubTypes.Type(value = GearConf.class,       name = "largegear"),
    @JsonSubTypes.Type(value = DriveChainConf.class, name = "drivechain"),
    @JsonSubTypes.Type(value = PaddleConf.class,     name = "paddle")
})

public abstract class ObjectConf {

    protected final float x;
    protected final float y;
    protected final float angle;
    protected final boolean staticFlag;

    protected ObjectConf(float x, float y, float angle, boolean staticFlag) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.staticFlag = staticFlag;
    }

    public float getX()         { return x; }
    public float getY()         { return y; }
    public float getAngle()     { return angle; }
    
    @JsonProperty("static")
    public boolean isStatic()   { return staticFlag; }

    public abstract String getSkinId();

    public void validate() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof ObjectConf)) return false;
        ObjectConf that = (ObjectConf) o;
        return Float.compare(that.x, x) == 0 &&
               Float.compare(that.y, y) == 0 &&
               Float.compare(that.angle, angle) == 0 &&
               staticFlag == that.staticFlag;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, angle, staticFlag);
    }
}
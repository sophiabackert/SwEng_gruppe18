package mm.domain;

import mm.domain.config.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ObjectConfTest {

    @Test
    void testTennisballConfBasicProperties() {
        TennisballConf conf = new TennisballConf(1.5f, 2.5f, 0.3f, false);
        assertEquals(1.5f, conf.getX());
        assertEquals(2.5f, conf.getY());
        assertEquals(0.3f, conf.getAngle());
        assertFalse(conf.isStatic());
        assertEquals("tennisball", conf.getSkinId());
    }

    @Test
    void testEqualsAndHashCodeForSameValues() {
        TennisballConf c1 = new TennisballConf(1,2,0.1f,false);
        TennisballConf c2 = new TennisballConf(1,2,0.1f,false);
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
        assertNotSame(c1, c2);
    }

    @Test
    void testNotEqualsForDifferentValues() {
        TennisballConf c1 = new TennisballConf(1,2,0.1f,false);
        TennisballConf c2 = new TennisballConf(1,2,0.2f,false);
        assertNotEquals(c1, c2);
    }

    @Test
    void testSubclassesHaveCorrectSkinId() {
        assertEquals("bowlingball", new BowlingballConf(0,0,0,false).getSkinId());
        assertEquals("billiardball", new BilliardballConf(0,0,0,false).getSkinId());
        assertEquals("balloon", new BalloonConf(0,0,0,false).getSkinId());
        assertEquals("gameball", new GameBallConf(0,0,0,false).getSkinId());
        assertEquals("log", new LogConf(0,0,0,false).getSkinId());
        assertEquals("plank", new PlankConf(0,0,0,false).getSkinId());
        assertEquals("domino", new DominoConf(0,0,0,false).getSkinId());
        assertEquals("cratebox", new CrateboxConf(0,0,0,false).getSkinId());
        assertEquals("bucket", new BucketConf(0,0,0,false).getSkinId());
        assertEquals("goalzone", new GoalZoneConf(0,0,0,true).getSkinId());
        assertEquals("restrictionzone", new RestrictionZoneConf(0,0,0,true).getSkinId());
    }

    @Test
    void testValidateDoesNotThrowForValidConfig() {
        assertDoesNotThrow(() -> new TennisballConf(0,0,0,false).validate());
    }
} 
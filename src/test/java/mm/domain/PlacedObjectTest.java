package mm.domain;

import javafx.scene.shape.Circle;
import mm.domain.config.TennisballConf;
import mm.domain.editor.PlacedObject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PlacedObjectTest {

    @Test
    void testConstructorAndGetters() {
        Circle c = new Circle(10);
        PlacedObject po = new PlacedObject(c, TennisballConf.class);
        assertSame(c, po.getNode());
        assertEquals(TennisballConf.class, po.getConfigClass());
    }

    @Test
    void testCopyCreatesDeepCopy() {
        Circle c = new Circle(10);
        c.setLayoutX(5);
        c.setLayoutY(7);
        PlacedObject po = new PlacedObject(c, TennisballConf.class);
        PlacedObject copy = po.copy();
        assertNotSame(po.getNode(), copy.getNode());
        assertEquals(po.getConfigClass(), copy.getConfigClass());
        Circle orig = (Circle) po.getNode();
        Circle cop = (Circle) copy.getNode();
        assertEquals(orig.getRadius(), cop.getRadius());
        assertEquals(orig.getLayoutX(), cop.getLayoutX());
        assertEquals(orig.getLayoutY(), cop.getLayoutY());
    }

    @Test
    void testToConfigReturnsCorrectType() {
        Circle c = new Circle(10);
        c.setLayoutX(100);
        c.setLayoutY(200);
        PlacedObject po = new PlacedObject(c, TennisballConf.class);
        var conf = po.toConfig();
        assertTrue(conf instanceof TennisballConf);
        assertEquals(1.0f, conf.getX(), 0.01);
        assertEquals(2.0f, conf.getY(), 0.01);
    }
} 
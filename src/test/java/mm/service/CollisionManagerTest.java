package mm.service;

import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import mm.domain.editor.PlacedObject;
import mm.domain.config.TennisballConf;
import mm.service.collision.CollisionManager;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class CollisionManagerTest {

    @Test
    void testCircleCircleCollision() {
        CollisionManager cm = new CollisionManager();
        Circle c1 = new Circle(10); c1.setLayoutX(50); c1.setLayoutY(50);
        Circle c2 = new Circle(10); c2.setLayoutX(60); c2.setLayoutY(50);
        PlacedObject po1 = new PlacedObject(c1, TennisballConf.class);
        PlacedObject po2 = new PlacedObject(c2, TennisballConf.class);
        List<PlacedObject> placed = new ArrayList<>();
        placed.add(po1); placed.add(po2);
        boolean overlap = cm.overlapsExisting(c1, null, placed, List.of());
        assertTrue(overlap);
    }

    @Test
    void testCircleRectangleNoCollision() {
        CollisionManager cm = new CollisionManager();
        Circle c1 = new Circle(10); c1.setLayoutX(10); c1.setLayoutY(10);
        Rectangle r = new Rectangle(20, 20); r.setLayoutX(100); r.setLayoutY(100);
        PlacedObject po1 = new PlacedObject(c1, TennisballConf.class);
        PlacedObject po2 = new PlacedObject(r, TennisballConf.class);
        List<PlacedObject> placed = new ArrayList<>();
        placed.add(po1); placed.add(po2);
        boolean overlap = cm.overlapsExisting(c1, null, placed, List.of());
        assertFalse(overlap);
    }
} 
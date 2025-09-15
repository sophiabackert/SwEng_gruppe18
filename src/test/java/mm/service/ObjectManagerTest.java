package mm.service;

import javafx.scene.layout.Pane;
import mm.domain.editor.PlacedObject;
import mm.service.object.ObjectManager;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class ObjectManagerTest {

    @Test
    void testAddAndRemovePlacedObject() {
        ObjectManager om = new ObjectManager(Map.of("tennisball", 2));
        Pane pane = new Pane();
        PlacedObject po = om.createPlacedObject("tennisball", 50, 50);
        assertNotNull(po);
        om.addPlacedObject(po, pane);
        assertTrue(om.getPlacedObjects().contains(po));
        om.removePlacedObject(po, pane);
        assertFalse(om.getPlacedObjects().contains(po));
    }

    @Test
    void testCheckLimitReached() {
        ObjectManager om = new ObjectManager(Map.of("tennisball", 1));
        PlacedObject po = om.createPlacedObject("tennisball", 10, 10);
        om.getPlacedObjects().add(po);
        assertTrue(om.checkLimitReached("tennisball"));
        assertFalse(om.checkLimitReached("bowlingball"));
    }

    @Test
    void testIsUniqueItemLimitReached() {
        ObjectManager om = new ObjectManager(Map.of("gameball", 1));
        PlacedObject po = om.createPlacedObject("gameball", 10, 10);
        om.getPlacedObjects().add(po);
        assertTrue(om.isUniqueItemLimitReached("gameball"));
        assertFalse(om.isUniqueItemLimitReached("tennisball"));
    }

    @Test
    void testClearRemovesAllObjects() {
        ObjectManager om = new ObjectManager();
        om.getPlacedObjects().add(om.createPlacedObject("tennisball", 10, 10));
        om.getPrePlacedObjects().add(om.createPlacedObject("bowlingball", 20, 20));
        om.clear();
        assertTrue(om.getPlacedObjects().isEmpty());
        assertTrue(om.getPrePlacedObjects().isEmpty());
    }

    @Test
    void testCreatePlacedObjectReturnsNullForUnknownType() {
        ObjectManager om = new ObjectManager();
        assertNull(om.createPlacedObject("unknown", 0, 0));
    }

    // Kollisionserkennung ist schwer zu testen ohne echte Objekte, daher hier nur ein einfacher Test:
    @Test
    void testIsWithinBoundsTrue() {
        ObjectManager om = new ObjectManager();
        Pane pane = new Pane();
        pane.setPrefSize(200, 200);
        PlacedObject po = om.createPlacedObject("tennisball", 100, 100);
        assertTrue(om.isWithinBounds(po.getNode(), pane));
    }
} 
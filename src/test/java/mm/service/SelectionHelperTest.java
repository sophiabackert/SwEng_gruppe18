package mm.service;

import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import mm.service.selection.SelectionHelper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SelectionHelperTest {

    @Test
    void testHighlightAndRemoveHighlight() {
        Circle c = new Circle(10);
        Pane pane = new Pane();
        SelectionHelper.highlightSelected(c, pane);
        assertEquals(3.0, c.getStrokeWidth());
        SelectionHelper.removeHighlight(c, pane);
        assertEquals(1.0, c.getStrokeWidth());
    }

    @Test
    void testClearSelectionReturnsNull() {
        Circle c = new Circle(10);
        Pane pane = new Pane();
        assertNull(SelectionHelper.clearSelection(pane, c));
    }

    @Test
    void testUpdateRotationButtons() {
        Button left = new Button();
        Button right = new Button();
        Circle c = new Circle(10);
        SelectionHelper.updateRotationButtons(left, right, c);
        assertFalse(left.isDisabled());
        assertFalse(right.isDisabled());
        SelectionHelper.updateRotationButtons(left, right, null);
        assertTrue(left.isDisabled());
        assertTrue(right.isDisabled());
    }
} 
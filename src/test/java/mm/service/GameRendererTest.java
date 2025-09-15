package mm.service;

import javafx.scene.canvas.Canvas;
import mm.service.rendering.GameRenderer;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class GameRendererTest {

    @Test
    void testRenderWithEmptyBodies() {
        Canvas canvas = new Canvas(200, 200);
        GameRenderer renderer = new GameRenderer(canvas);
        assertDoesNotThrow(() -> renderer.render(Map.of()));
    }
} 
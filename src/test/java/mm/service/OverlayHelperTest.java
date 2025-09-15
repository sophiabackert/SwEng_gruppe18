package mm.service;

import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import mm.service.overlay.OverlayHelper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OverlayHelperTest {

    @Test
    void testShowWarningAddsAndRemovesOverlay() throws Exception {
        StackPane pane = new StackPane();
        // JavaFX-Thread nötig
        Platform.startup(() -> {});
        try {
            Platform.runLater(() -> OverlayHelper.showWarning(pane, "Warnung", 1));
            Thread.sleep(100); // Warten, bis Overlay hinzugefügt wurde
            assertTrue(pane.getChildren().size() > 0);
            Thread.sleep(1200); // Warten, bis Overlay entfernt wurde
            assertEquals(0, pane.getChildren().size());
        } finally {
            Platform.exit();
        }
    }
} 
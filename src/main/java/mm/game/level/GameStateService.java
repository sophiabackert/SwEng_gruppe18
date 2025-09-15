package mm.game.level;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Game state service.
 */
public class GameStateService {

    private static final List<String> INVENTORY_IDS = List.of(
            "balken1", "balken2", "balken3", "balken4", "balken5",
            "balken6", "balken7", "balken8", "balken9", "balken10"
    );

    /**
     * Saves the current game state by collecting all beam elements
     * from the game pane and storing them in a JSON file ("saved_game.json").
     * @param gamePane the game pane
     */
    public static void saveGameState(Pane gamePane, String filename) {
        List<BeamData> beams = new ArrayList<>();

        for (Node node : gamePane.getChildren()) {
            if (node instanceof Rectangle rect && (rect.getId() == null || !INVENTORY_IDS.contains(rect.getId()))) {
                Bounds bounds = rect.getBoundsInParent();

                String colorString = "#cccccc";
                if (rect.getFill() instanceof Color color) {
                    colorString = String.format("#%02x%02x%02x",
                            (int) (color.getRed() * 255),
                            (int) (color.getGreen() * 255),
                            (int) (color.getBlue() * 255));
                }

                beams.add(new BeamData(
                        bounds.getMinX(),
                        bounds.getMinY(),
                        bounds.getWidth(),
                        bounds.getHeight(),
                        colorString
                ));
            }
        }
        File dir = new File("saved-levels");
        if (!dir.exists()) dir.mkdirs();

        try (Writer writer = new FileWriter(new File(dir, filename))) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(beams, writer);
            System.out.println("Spielstand gespeichert unter: " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Load game state.
     *
     * @param gamePane    the game pane
     * @param template    the template
     * @param dragHandler the drag handler
     */
    public static void loadGameState(Pane gamePane, Rectangle template, EnableDragHandler dragHandler, String filename) {
        GameStateService service = new GameStateService();
        BeamData[] beams = service.loadGameState(filename);

        if (beams != null) {
            gamePane.getChildren().removeIf(node -> {
                if (node instanceof Rectangle) {
                    String id = node.getId();
                    return id == null || !INVENTORY_IDS.contains(id);
                }
                return false;
            });

            for (BeamData beam : beams) {
                Rectangle rect = new Rectangle(beam.width, beam.height);
                rect.setLayoutX(beam.layoutX);
                rect.setLayoutY(beam.layoutY);
                rect.setArcHeight(5.0);
                rect.setArcWidth(5.0);
                rect.setStrokeType(template.getStrokeType());

                try {
                    rect.setFill(Color.web(beam.fillColor));
                } catch (Exception e) {
                    rect.setFill(Color.GRAY);
                }

                dragHandler.enableDrag(rect);
                gamePane.getChildren().add(rect);
            }

            System.out.println("Level geladen: " + filename);
        }
    }


    public BeamData[] loadGameState(String filename) {
        File file = new File("saved-levels/" + filename);
        if (!file.exists()) {
            System.err.println("Datei nicht gefunden: " + file.getAbsolutePath());
            return null;
        }

        try (Reader reader = new FileReader(file)) {
            Gson gson = new Gson();
            return gson.fromJson(reader, BeamData[].class);
        } catch (IOException e) {
            System.err.println("Fehler beim Laden: " + filename);
            e.printStackTrace();
            return null;
        }
    }


    /**
     * The interface Enable drag handler.
     */
    public interface EnableDragHandler {
        /**
         * Enable drag.
         *
         * @param rect the rect
         */
        void enableDrag(Rectangle rect);
    }


}




package mm.game.level;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class Level2 implements LevelControllerInterface {

    @FXML private AnchorPane gamePane;
    @FXML private Rectangle template;
    @FXML private BorderPane borderPane;
    @FXML private Rectangle placeBeam;
    @FXML private VBox inventoryBox;
    @FXML private AnchorPane bottomPane;
    @FXML private Group scaleGroup;
    @FXML private Group inventoryScaleGroup;
    @FXML private Button exitButton;
    @FXML private Button pauseButton;
    @FXML private Button resetButton;
    @FXML private Button playButton;

    private final double BASE_WIDTH = 600;
    private final double BASE_HEIGHT = 400;

    @FXML
    public void initialize() {
        //Größe des Spielfeldes dynamisch verändern
        inventoryBox.prefWidthProperty().bind(borderPane.widthProperty().multiply(0.20));
        bottomPane.prefWidthProperty().bind(borderPane.widthProperty());
        bottomPane.prefHeightProperty().bind(borderPane.heightProperty().multiply(0.1));

        // Skalierungsfaktor
        Scale gameScale = new Scale();
        scaleGroup.getTransforms().add(gameScale);
        Scale inventoryScale = new Scale();
        inventoryScaleGroup.getTransforms().add(inventoryScale);

        // Gemeinsamer Listener zur Größenanpassung
        borderPane.widthProperty().addListener((obs, oldVal, newVal) -> updateScale(gameScale, inventoryScale));
        borderPane.heightProperty().addListener((obs, oldVal, newVal) -> updateScale(gameScale, inventoryScale));

        //BottomPane Größe
        HBox.setHgrow(exitButton, Priority.ALWAYS);
        HBox.setHgrow(pauseButton, Priority.ALWAYS);
        HBox.setHgrow(resetButton, Priority.ALWAYS);
        HBox.setHgrow(playButton, Priority.ALWAYS);

        exitButton.setMaxWidth(Double.MAX_VALUE);
        pauseButton.setMaxWidth(Double.MAX_VALUE);
        resetButton.setMaxWidth(Double.MAX_VALUE);
        playButton.setMaxWidth(Double.MAX_VALUE);


        // Drag starten
        placeBeam.setOnDragDetected(event -> {
            Dragboard db = placeBeam.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.putString("beam");  // Marke setzen
            db.setContent(content);
            event.consume();
        });

        // Wenn etwas über das gamePane gezogen wird
        gamePane.setOnDragOver(event -> {
            if (event.getGestureSource() == placeBeam && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        // Beim Loslassen im gamePane
        gamePane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasString() && db.getString().equals("beam")) {
                // Skalierungsfaktor aus der Scene-Transformation
                double scale = scaleGroup.getLocalToSceneTransform().getMxx();

                // Ursprüngliche Größe hochskalieren
                double width = placeBeam.getWidth() * scale;
                double height = placeBeam.getHeight() * scale;

                Rectangle newBeam = new Rectangle(width, height);
                newBeam.setArcWidth(placeBeam.getArcWidth() * scale);
                newBeam.setArcHeight(placeBeam.getArcHeight() * scale);
                newBeam.setFill(placeBeam.getFill());
                newBeam.setStrokeType(placeBeam.getStrokeType());
                newBeam.getStyleClass().addAll(placeBeam.getStyleClass());

                newBeam.setLayoutX(event.getX());
                newBeam.setLayoutY(event.getY());

                enableDrag(newBeam); // Beweglich machen

                gamePane.getChildren().add(newBeam);
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
            event.consume();
        });
    }


    @FXML
    public void exitButtonOnAction(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/mm/gui/mainMenu.fxml"));
        Parent mainMenu = fxmlLoader.load();

        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(mainMenu);
        stage.setScene(scene);
        stage.show();
    }

    public void pauseButtonOnAction(ActionEvent actionEvent) throws IOException {
        String levelFilename = "level2.json";

        GameStateService.saveGameState(gamePane, levelFilename); //Spielstand speichern
        LevelStorage.setLastLevelFile(levelFilename);

        System.out.println("saved GameState " + levelFilename);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mm/gui/pauseScreen.fxml"));
        Parent game = loader.load();
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(game);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    @FXML
    private void resetButtonOnAction() {
        gamePane.getChildren().removeIf(node -> {
            if (node instanceof Rectangle) {
                Rectangle rect = (Rectangle) node;
                return rect.getId() == null || !List.of(
                        "balken1", "balken2", "balken3", "balken4", "balken5",
                        "balken6", "balken7", "balken8", "balken9", "balken10"
                ).contains(rect.getId());
            }
            return false;
        });
    }

    @FXML
    public void playButtonOnAction(ActionEvent actionEvent) {
        gamePane.setDisable(true);
    }

    private final GameStateService.EnableDragHandler dragHandler = rect -> {
        final Delta dragDelta = new Delta();

        rect.setOnMousePressed(event -> {
            dragDelta.x = rect.getLayoutX() - event.getSceneX();
            dragDelta.y = rect.getLayoutY() - event.getSceneY();
        });

        rect.setOnMouseDragged(event -> {
            rect.setLayoutX(event.getSceneX() + dragDelta.x);
            rect.setLayoutY(event.getSceneY() + dragDelta.y);
        });
    };

    private static class Delta {
        double x, y;
    }

    @Override
    public void loadLevel(String filename) {
        GameStateService.loadGameState(gamePane, template, dragHandler, filename);
    }

    public String levelToLoad;

    public void setLevelToLoad(String filename) {
        this.levelToLoad = filename;
    }

    public void initAfterLoad() {
        System.out.println("initAfterLoad called with levelToLoad = " + levelToLoad);
        if (levelToLoad != null) {
            loadLevel(levelToLoad);
        }
    }

    private void updateScale(Scale gameScale, Scale inventoryScale) {
        double scaleX = borderPane.getWidth() / BASE_WIDTH;
        double scaleY = borderPane.getHeight() / BASE_HEIGHT;
        double scaleFactor = Math.min(scaleX, scaleY);

        gameScale.setX(scaleFactor);
        gameScale.setY(scaleFactor);
        inventoryScale.setX(scaleFactor);
        inventoryScale.setY(scaleFactor);
    }

    public void enableDrag(Rectangle rect) {
        final double[] offsetX = new double[1];
        final double[] offsetY = new double[1];

        rect.setOnMousePressed(e -> {
            offsetX[0] = e.getX();
            offsetY[0] = e.getY();
        });

        rect.setOnMouseDragged(e -> {
            rect.setLayoutX(e.getSceneX() - gamePane.localToScene(gamePane.getBoundsInLocal()).getMinX() - offsetX[0]);
            rect.setLayoutY(e.getSceneY() - gamePane.localToScene(gamePane.getBoundsInLocal()).getMinY() - offsetY[0]);
        });
    }


}

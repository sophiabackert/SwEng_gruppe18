package mm.app;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import mm.core.physics.objects.*;               // Klassen: Plank, Log, Ball, Box, Domino, Balloon, Bucket

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.Body;

import java.util.HashMap;
import java.util.Map;

public class PhysicsSimulationTestApp extends Application {

    // Skalierung: 1 Meter in JBox2D = 50 Pixel in JavaFX
    private static final float SCALE = 50f;
    private static final float TIMESTEP = 1.0f / 60.0f;
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;

    private World world;
    private Map<Body, javafx.scene.shape.Shape> bodyShapeMap = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        // 1. Setup JavaFX-Pane
        Pane root = new Pane();
        root.setPrefSize(800, 600);

        // 2. Setup JBox2D-World mit Schwerkraft "nach unten"
        world = new World(new Vec2(0, -10));

        // 3. Erstelle alle Objekte und ihre JavaFX-Shapes
        addPlank   (  0, 0, 5.0f, 0.2f);
        addLog     (  2, 1, 0.5f);
        addBall    (  0, 4, 0.3f);
        addBox     (  1, 4);
        addDomino  (  2, 4);
        addBalloon (  0, 6, 0.4f);
        addBucket  (  3, 1, 1.0f, 0.5f);

        // 4. Alle Shapes ins Pane aufnehmen
        root.getChildren().addAll(bodyShapeMap.values());

        // 5. Scene / Stage anzeigen
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("MadMachines – Physics Simulation");
        primaryStage.show();

        // 6. Animations-Loop: physics.step() + JavaFX-Update
        new AnimationTimer() {
            private long last = 0;
            @Override
            public void handle(long now) {
                if (last > 0) {
                    world.step(TIMESTEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
                    updateJavaFXShapes();
                }
                last = now;
            }
        }.start();
    }

    private void updateJavaFXShapes() {
        double height = 600; // JavaFX-Pane-Höhe zum Umrechnen Y‐Achse
        for (Map.Entry<Body, javafx.scene.shape.Shape> e : bodyShapeMap.entrySet()) {
            Body b = e.getKey();
            javafx.scene.shape.Shape s = e.getValue();
            Vec2 p = b.getPosition();
            s.setTranslateX(p.x * SCALE);
            // invert Y
            s.setTranslateY(height - p.y * SCALE);
            s.setRotate(-Math.toDegrees(b.getAngle()));
        }
    }

    // ------- Helper zum Anlegen ------------------

    private void addPlank(float x, float y, float length, float thickness) {
        Body body = new Plank(x, y, length, thickness, 0f).createBody(world);
        Rectangle r = new Rectangle(length * SCALE, thickness * SCALE);
        r.setFill(Color.SADDLEBROWN);
        r.setStroke(Color.BLACK);
        bodyShapeMap.put(body, r);
    }

    private void addLog(float x, float y, float radius) {
        Body body = new Log(x, y, radius).createBody(world);
        Rectangle r = new Rectangle(radius*2 * SCALE, radius*2 * SCALE);
        r.setArcWidth(radius*2 * SCALE);
        r.setArcHeight(radius*2 * SCALE);
        r.setFill(Color.DARKGRAY);
        r.setStroke(Color.BLACK);
        bodyShapeMap.put(body, r);
    }

    private void addBall(float x, float y, float radius) {
        Body body = new Ball(x, y, radius, 1.0f, 0.3f).createBody(world);
        Circle c = new Circle(radius * SCALE);
        c.setFill(Color.LIGHTBLUE);
        c.setStroke(Color.BLACK);
        bodyShapeMap.put(body, c);
    }

    private void addBox(float x, float y) {
        Body body = new Box(x, y).createBody(world);
        // Da Box per Default 1×1 m groß ist, brauchen wir hier keine hx/hy mehr:
        Rectangle r = new Rectangle(1 * SCALE, 1 * SCALE);
        r.setFill(Color.GRAY);
        r.setStroke(Color.BLACK);
        bodyShapeMap.put(body, r);
    }

    private void addDomino(float x, float y) {
        Body body = new Domino(x, y).createBody(world);
        // Domino ist fest auf 0.1×0.8 m
        Rectangle r = new Rectangle(0.1f * 2 * SCALE, 0.8f * 2 * SCALE);
        r.setFill(Color.DARKORANGE);
        r.setStroke(Color.BLACK);
        bodyShapeMap.put(body, r);
    }

    private void addBalloon(float x, float y, float radius) {
        Body body = new Balloon(x, y, radius).createBody(world);
        Circle c = new Circle(radius * SCALE);
        c.setFill(Color.PINK);
        c.setStroke(Color.RED);
        bodyShapeMap.put(body, c);
    }

    private void addBucket(float x, float y, float width, float height) {
        Body body = new Bucket(x, y, width, height).createBody(world);
        Rectangle r = new Rectangle(width * SCALE, height * SCALE);
        r.setFill(Color.BEIGE);
        r.setStroke(Color.BROWN);
        bodyShapeMap.put(body, r);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
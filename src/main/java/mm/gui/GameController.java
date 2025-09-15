package mm.gui;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import mm.core.editor.PlacedObject;
import mm.core.config.*;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.dynamics.joints.*;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.collision.Manifold;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller für das Hauptspiel mit JBox2D Physiksimulation
 */
public class GameController extends Controller {
    
    @FXML private Canvas gameCanvas;
    @FXML private Label taskLabel;
    @FXML private Label timeLabel;
    @FXML private Button pauseButton;
    @FXML private Button exitButton;
    @FXML private StackPane overlayContainer;
    @FXML private VBox countdownOverlay;
    @FXML private VBox pauseOverlay;
    @FXML private VBox winOverlay;
    @FXML private VBox gameOverOverlay;
    @FXML private Label countdownLabel;

    // Physik-Welt
    private World world;
    private AnimationTimer gameLoop;
    
    // Skalierung: 1 Meter = 100 Pixel
    private static final float SCALE = 100.0f;
    private static final float TIME_STEP = 1.0f / 60.0f;
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;
    
    // Spiel-Zustand
    private boolean isRunning = false;
    private boolean isPaused = false;
    private boolean gameWon = false;
    private double gameTime = 60.0; // 60 Sekunden
    private String objective = "Bringe den Ball in die Zielzone";
    
    // Objekte im Spiel
    private Map<Body, RenderInfo> bodies = new HashMap<>();
    private List<Body> goalZones = new ArrayList<>();
    private Body gameBall; // Referenz für den Spielball
    private GearManager gearManager = new GearManager(); // Zahnrad-Manager
    
    // Countdown
    private double countdownTime = 3.0;
    private boolean showingCountdown = true;
    
    @FXML
    private void initialize() {
        setupKeyControls();
        setupPhysicsWorld();
    }
    
    /**
     * Initialisiert das Spiel mit Objekten vom Editor
     */
    public void initializeGame(List<PlacedObject> playerObjects, List<PlacedObject> levelObjects, String objective) {
        this.objective = objective;
        taskLabel.setText(objective);
        
        // Physik-Welt zurücksetzen
        setupPhysicsWorld();
        
        // Level-Objekte hinzufügen (statische Objekte aus der Level-Datei)
        for (PlacedObject po : levelObjects) {
            addObjectToWorld(po, true);
        }
        
        // Spieler-Objekte hinzufügen (vom Editor platzierte Objekte)
        for (PlacedObject po : playerObjects) {
            addObjectToWorld(po, false);
        }
        
        // Wände/Begrenzungen hinzufügen
        createWorldBounds();
        
        // Spiel starten
        startGame();
    }
    
    private void setupPhysicsWorld() {
        // Schwerkraft nach unten
        Vec2 gravity = new Vec2(0.0f, 9.8f);
        world = new World(gravity);
        
        // Collision-Listener für Ziel-Erkennung
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                checkGoalContact(contact);
                // Zahnrad-Kontakte prüfen
                gearManager.handleGearContact(contact, true);
            }
            
            @Override 
            public void endContact(Contact contact) {
                // Zahnrad-Kontakte beenden
                gearManager.handleGearContact(contact, false);
            }
            
            @Override public void preSolve(Contact contact, Manifold oldManifold) {}
            @Override public void postSolve(Contact contact, ContactImpulse impulse) {}
        });
    }
    
    private void addObjectToWorld(PlacedObject placedObject, boolean fromLevel) {
        ObjectConf config = placedObject.toConfig();
        Body body = null;
        
        // Position in Box2D-Koordinaten umrechnen
        float x = config.getX();
        float y = config.getY();
        float angle = config.getAngle();
        boolean isStatic = config.isStatic(); // Level-Objekte sind nur statisch wenn explizit in Config definiert
        
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = isStatic ? BodyType.STATIC : BodyType.DYNAMIC;
        bodyDef.position.set(x, y);
        bodyDef.angle = angle;
        
        body = world.createBody(bodyDef);
        
        // Form und Eigenschaften basierend auf Objekttyp
        if (config instanceof BallConf) {
            BallConf ballConf = (BallConf) config;
            createCircleFixture(body, ballConf);
            
            // Spezielle Behandlung für verschiedene Ball-Typen
            if (ballConf instanceof BalloonConf) {
                // Ballons bekommen Auftrieb-Tag für spätere Behandlung
                body.setUserData("balloon");
            } else if (ballConf instanceof GameBallConf) {
                // Spielball markieren - dieser muss die Goalzone erreichen
                body.setUserData("gameball");
                gameBall = body; // Referenz für Gewinnprüfung speichern
            }
            
            // Render-Info speichern
            bodies.put(body, new RenderInfo(RenderType.CIRCLE, ballConf.getRadius(), 0, 0, getColorForSkin(ballConf.getSkinId())));
            
        } else if (config instanceof BoxConf) {
            BoxConf boxConf = (BoxConf) config;
            createBoxFixture(body, boxConf);
            
            // Render-Info speichern
            bodies.put(body, new RenderInfo(RenderType.BOX, boxConf.getWidth(), boxConf.getHeight(), 0, getColorForSkin(boxConf.getSkinId())));
            
        } else if (config instanceof BucketConf) {
            BucketConf bucketConf = (BucketConf) config;
            createBucketFixture(body, bucketConf);
            
            // Render-Info speichern
            bodies.put(body, new RenderInfo(RenderType.BUCKET, bucketConf.getWidth(), bucketConf.getHeight(), bucketConf.getThickness(), getColorForSkin(bucketConf.getSkinId())));
            
        } else if (config instanceof GoalZoneConf) {
            GoalZoneConf goalConf = (GoalZoneConf) config;
            createGoalZoneFixture(body, goalConf);
            
            // Goalzone markieren und zur Liste hinzufügen
            body.setUserData("goalzone");
            goalZones.add(body);
            
            // Render-Info speichern
            bodies.put(body, new RenderInfo(RenderType.GOALZONE, goalConf.getWidth(), goalConf.getHeight(), 0, getColorForSkin(goalConf.getSkinId())));
            
        } else if (config instanceof GearConf) {
            GearConf gearConf = (GearConf) config;
            createGearFixture(body, gearConf);
            
            // Zahnrad beim GearManager registrieren
            gearManager.addGear(body, gearConf);
            
            // Render-Info speichern
            bodies.put(body, new RenderInfo(RenderType.GEAR, gearConf.getInnerRadius(), gearConf.getOuterRadius(), 0, getColorForSkin(gearConf.getSkinId())));
            
        } else if (config instanceof DriveChainConf) {
            DriveChainConf chainConf = (DriveChainConf) config;
            
            // Antriebsstrang benötigt keinen eigenen Physik-Body, 
            // nur eine Verbindung zwischen den referenzierten Zahnrädern
            // Wir müssen die Zahnräder anhand ihrer IDs finden
            Body gearA = findGearById(chainConf.getGearAId());
            Body gearB = findGearById(chainConf.getGearBId());
            
            if (gearA != null && gearB != null) {
                gearManager.addDriveChain(gearA, gearB, chainConf);
                System.out.println("Antriebsstrang zwischen Zahnrädern erstellt");
            } else {
                System.err.println("Warnung: Zahnräder für Antriebsstrang nicht gefunden - IDs: " + 
                                 chainConf.getGearAId() + ", " + chainConf.getGearBId());
            }
            
            // Antriebsstrang wird nur visuell gerendert, kein eigener Body
            return;
        }
    }
    
    private Body findGearById(String id) {
        for (Map.Entry<Body, RenderInfo> entry : bodies.entrySet()) {
            Body body = entry.getKey();
            if (body.getUserData() != null && body.getUserData().equals(id)) {
                return body;
            }
        }
        return null;
    }
    
    private void createCircleFixture(Body body, BallConf config) {
        CircleShape circle = new CircleShape();
        circle.m_radius = config.getRadius();
        
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = config.getDensity();
        fixtureDef.friction = config.getFriction();
        fixtureDef.restitution = config.getRestitution();
        
        body.createFixture(fixtureDef);
    }
    
    private void createBoxFixture(Body body, BoxConf config) {
        PolygonShape box = new PolygonShape();
        box.setAsBox(config.getWidth() / 2, config.getHeight() / 2);
        
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = box;
        fixtureDef.density = config.getDensity();
        fixtureDef.friction = config.getFriction();
        fixtureDef.restitution = config.getRestitution();
        
        body.createFixture(fixtureDef);
    }
    
    private void createBucketFixture(Body body, BucketConf config) {
        float width = config.getWidth();
        float height = config.getHeight();
        float thickness = config.getThickness();
        float wallAngle = config.getWallAngle();
        
        // Boden (horizontal)
        PolygonShape bottom = new PolygonShape();
        bottom.setAsBox(width / 2, thickness / 2, new Vec2(0, 0), 0);
        
        FixtureDef bottomFixture = new FixtureDef();
        bottomFixture.shape = bottom;
        bottomFixture.density = 0.8f;
        bottomFixture.friction = 0.6f;
        bottomFixture.restitution = 0.1f;
        body.createFixture(bottomFixture);
        
        // Linke Wand (schräg)
        PolygonShape leftWall = new PolygonShape();
        float wallLength = height / (float)Math.sin(wallAngle);
        leftWall.setAsBox(thickness / 2, wallLength / 2, 
                         new Vec2(-width / 2 - thickness / 2, -wallLength / 2), 
                         -(float)Math.PI / 2 + wallAngle);
        
        FixtureDef leftFixture = new FixtureDef();
        leftFixture.shape = leftWall;
        leftFixture.density = 0.8f;
        leftFixture.friction = 0.6f;
        leftFixture.restitution = 0.1f;
        body.createFixture(leftFixture);
        
        // Rechte Wand (schräg)
        PolygonShape rightWall = new PolygonShape();
        rightWall.setAsBox(thickness / 2, wallLength / 2,
                          new Vec2(width / 2 + thickness / 2, -wallLength / 2),
                          (float)Math.PI / 2 - wallAngle);
        
        FixtureDef rightFixture = new FixtureDef();
        rightFixture.shape = rightWall;
        rightFixture.density = 0.8f;
        rightFixture.friction = 0.6f;
        rightFixture.restitution = 0.1f;
        body.createFixture(rightFixture);
    }
    
    private void createGoalZoneFixture(Body body, GoalZoneConf config) {
        float width = config.getWidth();
        float height = config.getHeight();
        
        PolygonShape goalZoneShape = new PolygonShape();
        goalZoneShape.setAsBox(width / 2, height / 2, new Vec2(0, 0), 0);
        
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = goalZoneShape;
        fixtureDef.density = 0.0f; // Statische Zone, keine Masse
        fixtureDef.friction = 0.0f;
        fixtureDef.restitution = 0.0f;
        fixtureDef.isSensor = true; // Sensor-Fixture, um Kollisionen zu erkennen
        
        body.createFixture(fixtureDef);
    }
    
    private void createGearFixture(Body body, GearConf config) {
        // Innerer Sensor - für Zahnrad-zu-Zahnrad Erkennung
        CircleShape innerSensor = new CircleShape();
        innerSensor.m_radius = config.getInnerRadius();
        
        FixtureDef innerFixture = new FixtureDef();
        innerFixture.shape = innerSensor;
        innerFixture.isSensor = true;
        
        body.createFixture(innerFixture);
        
        // Äußerer Kollisionskörper - für normale Objekte
        CircleShape outerBody = new CircleShape();
        outerBody.m_radius = config.getOuterRadius();
        
        FixtureDef outerFixture = new FixtureDef();
        outerFixture.shape = outerBody;
        outerFixture.isSensor = false;
        
        body.createFixture(outerFixture);
    }
    
    private void createWorldBounds() {
        // Canvas-Größe in Meter
        float canvasWidth = (float)(gameCanvas.getWidth() / SCALE);
        float canvasHeight = (float)(gameCanvas.getHeight() / SCALE);
        
        // Unsichtbare Wände an den Rändern
        createWall(canvasWidth / 2, -0.1f, canvasWidth, 0.2f, false); // Oben
        createWall(canvasWidth / 2, canvasHeight + 0.1f, canvasWidth, 0.2f, true); // Unten (Boden)
        createWall(-0.1f, canvasHeight / 2, 0.2f, canvasHeight, false); // Links
        createWall(canvasWidth + 0.1f, canvasHeight / 2, 0.2f, canvasHeight, false); // Rechts
    }
    
    private void createWall(float x, float y, float width, float height, boolean isFloor) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.STATIC;
        bodyDef.position.set(x, y);
        
        Body body = world.createBody(bodyDef);
        
        PolygonShape box = new PolygonShape();
        box.setAsBox(width / 2, height / 2);
        
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = box;
        
        if (isFloor) {
            // Boden hat leichte Reibung um Objekte langsam zur Ruhe kommen zu lassen
            fixtureDef.friction = 0.05f;
            fixtureDef.restitution = 0.2f;
        } else {
            // Wände haben normale Reibung
            fixtureDef.friction = 0.3f;
            fixtureDef.restitution = 0.2f;
        }
        
        body.createFixture(fixtureDef);
    }
    
    private void checkGoalContact(Contact contact) {
        Body bodyA = contact.getFixtureA().getBody();
        Body bodyB = contact.getFixtureB().getBody();
        
        // Prüfe ob Spielball die Goalzone berührt
        boolean gameballTouchesGoal = false;
        
        if (isGameBall(bodyA) && isGoalZone(bodyB)) {
            gameballTouchesGoal = true;
        } else if (isGameBall(bodyB) && isGoalZone(bodyA)) {
            gameballTouchesGoal = true;
        }
        
        if (gameballTouchesGoal && !gameWon) {
            gameWon = true;
            System.out.println("Spielball hat die Goalzone erreicht - Gewonnen!");
            Platform.runLater(this::showWinOverlay);
        }
    }
    
    private boolean isGameBall(Body body) {
        Object userData = body.getUserData();
        return "gameball".equals(userData);
    }
    
    private boolean isGoalZone(Body body) {
        Object userData = body.getUserData();
        return "goalzone".equals(userData);
    }
    
    private Color getColorForSkin(String skinId) {
        switch (skinId) {
            case "tennisball": return Color.LIME; // Leuchtend grün-gelb
            case "bowlingball": return Color.web("#2C3E50"); // Dunkles Blau-Grau
            case "billiardball": return Color.web("#ECF0F1"); // Perlweiß
            case "balloon": return Color.web("#E91E63"); // Magenta-Pink
            case "log": return Color.web("#8B4513"); // Sattbraun
            case "plank": return Color.web("#D2691E"); // Schokoladenbraun
            case "domino": return Color.web("#95A5A6"); // Silber-Grau
            case "cratebox": return Color.web("#FF6347"); // Tomatenrot
            case "bucket": return Color.web("#3498DB"); // Brillantblau
            case "gameball": return Color.ORANGE; // Orange für Spielball
            case "goalzone": return Color.LIGHTGREEN.deriveColor(0, 1, 1, 0.3); // Transparentes Grün
            case "smallgear": return Color.SILVER; // Silber für kleine Zahnräder
            case "largegear": return Color.DARKGRAY; // Dunkelgrau für große Zahnräder
            default: return Color.web("#7F8C8D"); // Dunkles Grau
        }
    }
    
    private void startGame() {
        isRunning = true;
        SettingsController.resetFPSTimer(); // Timer zurücksetzen
        showCountdown();
        
        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;
            private long lastCountdownUpdate = 0;
            
            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    lastCountdownUpdate = now;
                    render(); // Erstes Rendering
                    return;
                }
                
                double deltaTime = (now - lastUpdate) * 1e-9; // Umrechnung in Sekunden
                
                // Countdown läuft unabhängig von FPS-Limitierung
                if (showingCountdown) {
                    double countdownDelta = (now - lastCountdownUpdate) * 1e-9;
                    lastCountdownUpdate = now;
                    
                    countdownTime -= countdownDelta;
                    if (countdownTime <= 0) {
                        hideCountdown();
                    }
                    
                    // Countdown-Text aktualisieren
                    Platform.runLater(() -> {
                        if (countdownTime > 0) {
                            countdownLabel.setText(String.valueOf((int) Math.ceil(countdownTime)));
                        } else {
                            countdownLabel.setText("LOS!");
                        }
                    });
                    
                    render(); // Countdown immer rendern
                    return;
                }
                
                // FPS-Check nur für Game-Updates, nicht für Countdown
                if (!SettingsController.shouldUpdate()) {
                    return;
                }
                
                lastUpdate = now;
                
                if (!isPaused) {
                    // Zeit-Update mit Slow-Motion
                    double scaledDeltaTime = deltaTime * SettingsController.getTimeScale();
                    update(scaledDeltaTime);
                }
                
                render();
            }
        };
        gameLoop.start();
    }
    
    private void update(double deltaTime) {
        // Spielzeit aktualisieren
        gameTime -= deltaTime;
        if (gameTime <= 0) {
            gameTime = 0;
            showGameOverOverlay();
            isRunning = false;
        }
        
        // Zeit-Label aktualisieren
        Platform.runLater(() -> {
            timeLabel.setText(String.format("%.1f", gameTime));
        });
        
        // Physik-Simulation mit Slow-Motion
        float timeStep = TIME_STEP * (float)SettingsController.getTimeScale();
        world.step(timeStep, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
        
        // Zahnrad-Rotationen aktualisieren
        gearManager.updateGearRotations();
        
        // Auftrieb für Ballons
        applyBalloonBuoyancy();
    }
    
    private void applyBalloonBuoyancy() {
        // Auftriebskraft für Ballons (stärker als Schwerkraft)
        Vec2 buoyancyForce = new Vec2(0.0f, -12.0f); // Nach oben, stärker als Schwerkraft (9.8)
        
        for (Body body : bodies.keySet()) {
            if ("balloon".equals(body.getUserData())) {
                // Auftrieb anwenden, proportional zur Masse
                float mass = body.getMass();
                Vec2 scaledForce = buoyancyForce.mul(mass);
                body.applyForceToCenter(scaledForce);
            }
        }
    }
    
    private void render() {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gc.save();
        
        // Canvas löschen
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        
        // Koordinatensystem: 0,0 in der Mitte, y nach unten
        gc.translate(gameCanvas.getWidth() / 2, gameCanvas.getHeight() / 2);
        
        // Schatten für alle Objekte
        gc.setGlobalAlpha(0.2);
        gc.translate(5, 5);
        for (Map.Entry<Body, RenderInfo> entry : bodies.entrySet()) {
            Body body = entry.getKey();
            RenderInfo info = entry.getValue();
            
            gc.save();
            Vec2 position = body.getPosition();
            gc.translate(position.x * SCALE, position.y * SCALE);
            gc.rotate(Math.toDegrees(body.getAngle()));
            
            gc.setFill(Color.BLACK);
            renderObjectShape(gc, info, true);
            gc.restore();
        }
        
        // Schatten für Antriebsstränge
        for (GearManager.DriveChainInfo chain : gearManager.getDriveChains()) {
            Vec2 posA = chain.gearA.getPosition();
            Vec2 posB = chain.gearB.getPosition();
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(chain.config.getThickness() * SCALE);
            gc.strokeLine(
                posA.x * SCALE,
                posA.y * SCALE,
                posB.x * SCALE,
                posB.y * SCALE
            );
        }
        
        // Zurück zur Original-Position
        gc.translate(-5, -5);
        gc.setGlobalAlpha(1.0);
        
        // Alle Objekte normal rendern
        for (Map.Entry<Body, RenderInfo> entry : bodies.entrySet()) {
            Body body = entry.getKey();
            RenderInfo info = entry.getValue();
            
            gc.save();
            Vec2 position = body.getPosition();
            gc.translate(position.x * SCALE, position.y * SCALE);
            gc.rotate(Math.toDegrees(body.getAngle()));
            
            // Hauptform
            gc.setFill(info.color);
            gc.setStroke(info.color.darker());
            gc.setLineWidth(1);
            renderObjectShape(gc, info, false);
            
            // Glanzeffekt
            gc.setFill(Color.WHITE);
            gc.setGlobalAlpha(0.3);
            renderGloss(gc, info);
            gc.setGlobalAlpha(1.0);
            
            gc.restore();
        }
        
        // Antriebsstränge rendern
        for (GearManager.DriveChainInfo chain : gearManager.getDriveChains()) {
            renderDriveChain(gc, chain.gearA, chain.gearB, chain.config.getThickness());
        }
        
        gc.restore();
    }
    
    private void renderObjectShape(GraphicsContext gc, RenderInfo info, boolean isShadow) {
        switch (info.type) {
            case CIRCLE:
                double radius = info.param1 * SCALE;
                gc.fillOval(-radius, -radius, radius * 2, radius * 2);
                if (!isShadow) {
                    gc.strokeOval(-radius, -radius, radius * 2, radius * 2);
                }
                break;
                
            case BOX:
                double width = info.param1 * SCALE;
                double height = info.param2 * SCALE;
                gc.fillRect(-width / 2, -height / 2, width, height);
                if (!isShadow) {
                    gc.strokeRect(-width / 2, -height / 2, width, height);
                }
                break;
                
            case BUCKET:
                if (!isShadow) {
                    renderBucket(gc, info);
                } else {
                    // Vereinfachter Schatten für Bucket
                    double w = info.param1 * SCALE;
                    double h = info.param2 * SCALE;
                    gc.fillRect(-w / 2, -h / 2, w, h / 4);
                }
                break;
            case GOALZONE:
                double goalWidth = info.param1 * SCALE;
                double goalHeight = info.param2 * SCALE;
                gc.fillRect(-goalWidth / 2, -goalHeight / 2, goalWidth, goalHeight);
                if (!isShadow) {
                    gc.strokeRect(-goalWidth / 2, -goalHeight / 2, goalWidth, goalHeight);
                }
                break;
                
            case GEAR:
                if (!isShadow) {
                    renderGear(gc, info);
                } else {
                    // Vereinfachter Schatten für Zahnrad
                    double gearRadius = info.param1 * SCALE; // Innerer Radius für Schatten
                    gc.fillOval(-gearRadius, -gearRadius, gearRadius * 2, gearRadius * 2);
                }
                break;
        }
    }
    
    private void renderObjectWithGradient(GraphicsContext gc, RenderInfo info) {
        Color baseColor = info.color;
        Color lightColor = baseColor.brighter().brighter();
        Color darkColor = baseColor.darker();
        
        switch (info.type) {
            case CIRCLE:
                double radius = info.param1 * SCALE;
                
                // Radialer Gradient für 3D-Effekt
                var circleGradient = new javafx.scene.paint.RadialGradient(
                    0, 0, -radius * 0.3, -radius * 0.3, radius * 1.2, false,
                    javafx.scene.paint.CycleMethod.NO_CYCLE,
                    new javafx.scene.paint.Stop(0, lightColor),
                    new javafx.scene.paint.Stop(0.7, baseColor),
                    new javafx.scene.paint.Stop(1, darkColor)
                );
                gc.setFill(circleGradient);
                gc.fillOval(-radius, -radius, radius * 2, radius * 2);
                
                // Rand
                gc.setStroke(darkColor.darker());
                gc.setLineWidth(2);
                gc.strokeOval(-radius, -radius, radius * 2, radius * 2);
                break;
                
            case BOX:
                double width = info.param1 * SCALE;
                double height = info.param2 * SCALE;
                
                // Linearer Gradient für Boxen
                var boxGradient = new javafx.scene.paint.LinearGradient(
                    0, 0, 0, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                    new javafx.scene.paint.Stop(0, lightColor),
                    new javafx.scene.paint.Stop(0.5, baseColor),
                    new javafx.scene.paint.Stop(1, darkColor)
                );
                gc.setFill(boxGradient);
                gc.fillRect(-width / 2, -height / 2, width, height);
                
                // Rand
                gc.setStroke(darkColor.darker());
                gc.setLineWidth(2);
                gc.strokeRect(-width / 2, -height / 2, width, height);
                break;
                
            case BUCKET:
                renderBucket(gc, info);
                break;
        }
    }
    
    private void renderGloss(GraphicsContext gc, RenderInfo info) {
        switch (info.type) {
            case CIRCLE:
                double radius = info.param1 * SCALE;
                // Kleiner Glanz-Punkt
                double glossSize = radius * 0.4;
                gc.fillOval(-radius * 0.3, -radius * 0.3, glossSize, glossSize);
                break;
                
            case BOX:
                double width = info.param1 * SCALE;
                double height = info.param2 * SCALE;
                // Glanz-Streifen oben
                gc.fillRect(-width / 2, -height / 2, width, height * 0.2);
                break;
                
            case GEAR:
                // Glanz auf dem Zahnrad-Zentrum
                double gearRadius = info.param1 * SCALE;
                double gearGlossSize = gearRadius * 0.4;
                gc.fillOval(-gearRadius * 0.3, -gearRadius * 0.3, gearGlossSize, gearGlossSize);
                break;
        }
    }
    
    private void renderGear(GraphicsContext gc, RenderInfo info) {
        double innerRadius = info.param1 * SCALE;
        double outerRadius = info.param2 * SCALE;
        
        // Äußerer Kreis
        gc.setFill(info.color);
        gc.fillOval(-outerRadius, -outerRadius, outerRadius * 2, outerRadius * 2);
        gc.setStroke(info.color.darker());
        gc.setLineWidth(2);
        gc.strokeOval(-outerRadius, -outerRadius, outerRadius * 2, outerRadius * 2);
        
        // Innerer Kreis (heller)
        gc.setFill(info.color.brighter());
        gc.fillOval(-innerRadius, -innerRadius, innerRadius * 2, innerRadius * 2);
        gc.setStroke(info.color.darker());
        gc.setLineWidth(1);
        gc.strokeOval(-innerRadius, -innerRadius, innerRadius * 2, innerRadius * 2);
    }
    
    /**
     * Rendert einen Antriebsstrang zwischen zwei Zahnrädern
     */
    private void renderDriveChain(GraphicsContext gc, Body gearA, Body gearB, float thickness) {
        // Positionen der Zahnräder
        Vec2 posA = gearA.getPosition();
        Vec2 posB = gearB.getPosition();
        
        // In Pixel-Koordinaten umrechnen
        double x1 = posA.x * SCALE;
        double y1 = posA.y * SCALE;
        double x2 = posB.x * SCALE;
        double y2 = posB.y * SCALE;
        
        // Gestrichelte Linie zeichnen
        gc.save();
        gc.setLineDashes(5); // Gestrichelte Linie
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(thickness * SCALE);
        gc.strokeLine(x1, y1, x2, y2);
        gc.restore();
        
        // Kleine Kettenrad-Symbole an den Enden
        double symbolSize = thickness * SCALE * 3;
        renderChainSymbol(gc, x1, y1, symbolSize);
        renderChainSymbol(gc, x2, y2, symbolSize);
    }
    
    /**
     * Rendert ein kleines Kettenrad-Symbol
     */
    private void renderChainSymbol(GraphicsContext gc, double x, double y, double size) {
        gc.save();
        gc.translate(x, y);
        
        // Äußerer Kreis
        gc.setFill(Color.DARKGRAY);
        gc.fillOval(-size/2, -size/2, size, size);
        
        // Innerer Kreis
        gc.setFill(Color.LIGHTGRAY);
        gc.fillOval(-size/4, -size/4, size/2, size/2);
        
        gc.restore();
    }
    
    private void renderBucket(GraphicsContext gc, RenderInfo info) {
        double width = info.param1 * SCALE;
        double height = info.param2 * SCALE;
        double thickness = info.param3 * SCALE;
        
        gc.setStroke(info.color);
        gc.setLineWidth(thickness);
        
        // Boden
        gc.strokeLine(-width / 2, 0, width / 2, 0);
        
        // Seitenwände (vereinfacht als gerade Linien)
        gc.strokeLine(-width / 2, 0, -width / 2 - height * 0.2, -height);
        gc.strokeLine(width / 2, 0, width / 2 + height * 0.2, -height);
    }
    
    private void setupKeyControls() {
        gameCanvas.setFocusTraversable(true);
        gameCanvas.setOnKeyPressed(this::handleKeyPressed);
    }
    
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.SPACE) {
            handlePause();
        } else if (event.getCode() == KeyCode.R) {
            handleRestart();
        }
    }
    
    // UI Event Handler
    @FXML
    private void handlePause() {
        if (!isPaused) {
            isPaused = true;
            showPauseOverlay();
        }
    }
    
    @FXML
    private void handleResume() {
        isPaused = false;
        hidePauseOverlay();
    }
    
    @FXML
    private void handleRestart() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        
        // Neustart des Spiels - zurück zum Editor mit wiederhergestelltem Zustand
        if (viewManager != null) {
            viewManager.showGameEditor();
            // Hole den GameEditorController und stelle den Zustand wieder her
            Object controller = viewManager.getLastController();
            if (controller instanceof GameEditorController) {
                GameEditorController gameEditorController = (GameEditorController) controller;
                gameEditorController.restoreState();
            }
        }
    }
    
    @FXML
    private void handleBack() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        
        // Zurück zum Editor mit wiederhergestelltem Zustand (nicht Level Selection)
        if (viewManager != null) {
            viewManager.showGameEditor();
            // Hole den GameEditorController und stelle den Zustand wieder her
            Object controller = viewManager.getLastController();
            if (controller instanceof GameEditorController) {
                GameEditorController gameEditorController = (GameEditorController) controller;
                gameEditorController.restoreState();
            }
        }
    }
    
    @FXML
    private void handleToLevelSelection() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        
        // Zur Level-Auswahl zurückkehren
        if (viewManager != null) {
            viewManager.showLevelSelection();
        }
    }
    
    // Overlay-Management
    private void showCountdown() {
        overlayContainer.setVisible(true);
        countdownOverlay.setVisible(true);
        showingCountdown = true;
        countdownTime = 3.0;
    }
    
    private void hideCountdown() {
        countdownOverlay.setVisible(false);
        overlayContainer.setVisible(false);
        showingCountdown = false;
    }
    
    private void showPauseOverlay() {
        overlayContainer.setVisible(true);
        pauseOverlay.setVisible(true);
    }
    
    private void hidePauseOverlay() {
        pauseOverlay.setVisible(false);
        overlayContainer.setVisible(false);
    }
    
    private void showWinOverlay() {
        isRunning = false;
        overlayContainer.setVisible(true);
        winOverlay.setVisible(true);
    }
    
    private void showGameOverOverlay() {
        isRunning = false;
        overlayContainer.setVisible(true);
        gameOverOverlay.setVisible(true);
    }
    
    // Hilfklassen
    private enum RenderType {
        CIRCLE, BOX, BUCKET, GOALZONE, GEAR
    }
    
    private static class RenderInfo {
        final RenderType type;
        final double param1; // radius oder width
        final double param2; // height
        final double param3; // thickness (für Bucket)
        final Color color;
        
        RenderInfo(RenderType type, double param1, double param2, double param3, Color color) {
            this.type = type;
            this.param1 = param1;
            this.param2 = param2;
            this.param3 = param3;
            this.color = color;
        }
    }
} 
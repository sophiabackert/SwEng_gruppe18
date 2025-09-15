package mm.service.physics;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import mm.domain.editor.PlacedObject;
import mm.domain.config.ObjectConf;
import mm.domain.config.BallConf;
import mm.domain.config.BalloonConf;
import mm.domain.config.GameBallConf;
import mm.domain.config.BoxConf;
import mm.domain.config.BucketConf;
import mm.domain.config.GoalZoneConf;
import mm.domain.config.RestrictionZoneConf;
import mm.service.rendering.GameRenderer;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.collision.Manifold;
import javafx.application.Platform;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Verwaltet die Physiksimulation mit JBox2D für das Spiel.
 * <p>
 * Erstellt und verwaltet die physikalische Welt, fügt Objekte hinzu, prüft Kollisionen (z.B. Siegbedingung),
 * steuert die Weltgrenzen und spezielle Effekte wie Ballon-Auftrieb.
 * </p>
 */
public class PhysicsManager {
    
    private static final float SCALE = 100.0f;
    private static final float TIME_STEP = 1.0f / 60.0f;
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;
    
    private World world;
    private Canvas gameCanvas;
    private Map<Body, GameRenderer.RenderInfo> bodies = new HashMap<>();
    private List<Body> goalZones = new ArrayList<>();
    private List<Body> restrictionZones = new ArrayList<>();
    
    private boolean gameWon = false;
    private Runnable onGameWon;
    
    /**
     * Erstellt einen neuen PhysicsManager für das angegebene Canvas.
     * @param gameCanvas Zeichenfläche für das Spiel
     */
    public PhysicsManager(Canvas gameCanvas) {
        this.gameCanvas = gameCanvas;
        setupPhysicsWorld();
    }
    
    /**
     * Setzt den Callback, der bei Spielgewinn ausgeführt wird.
     * @param onGameWon Runnable für Sieg
     */
    public void setOnGameWon(Runnable onGameWon) {
        this.onGameWon = onGameWon;
    }
    
    /**
     * Gibt die aktuelle JBox2D-Welt zurück.
     * @return World-Objekt
     */
    public World getWorld() {
        return world;
    }
    
    /**
     * Gibt die Map der Körper und Render-Infos zurück.
     * @return Map Body → RenderInfo
     */
    public Map<Body, GameRenderer.RenderInfo> getBodies() {
        return bodies;
    }
    
    /**
     * Gibt zurück, ob das Spiel gewonnen wurde.
     * @return true, wenn gewonnen
     */
    public boolean isGameWon() {
        return gameWon;
    }
    
    /**
     * Führt einen Simulationsschritt aus.
     */
    public void step() {
        world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
    }
    
    private void setupPhysicsWorld() {
        Vec2 gravity = new Vec2(0.0f, 9.8f);
        world = new World(gravity);
        
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                checkGoalContact(contact);
            }
            
            @Override public void endContact(Contact contact) {}
            @Override public void preSolve(Contact contact, Manifold oldManifold) {}
            @Override public void postSolve(Contact contact, ContactImpulse impulse) {}
        });
    }
    
    /**
     * Fügt ein Objekt der Physikwelt hinzu.
     * @param placedObject Zu platzierendes Objekt
     * @param fromLevel true, wenn aus Level geladen
     */
    public void addObjectToWorld(PlacedObject placedObject, boolean fromLevel) {
        ObjectConf config = placedObject.toConfig();
        Body body = null;
        
        float x = config.getX();
        float y = config.getY();
        float angle = config.getAngle();
        boolean isStatic = config.isStatic();
        
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = isStatic ? BodyType.STATIC : BodyType.DYNAMIC;
        bodyDef.position.set(x, y);
        bodyDef.angle = angle;
        
        body = world.createBody(bodyDef);
        
        if (config instanceof BallConf) {
            BallConf ballConf = (BallConf) config;
            createCircleFixture(body, ballConf);
            
            if (ballConf instanceof BalloonConf) {
                body.setUserData("balloon");
            } else if (ballConf instanceof GameBallConf) {
                body.setUserData("gameball");
            }
            
            bodies.put(body, new GameRenderer.RenderInfo(GameRenderer.RenderType.CIRCLE, ballConf.getRadius(), 0, 0, getColorForSkin(ballConf.getSkinId()), ballConf.getSkinId()));
            
        } else if (config instanceof BoxConf) {
            BoxConf boxConf = (BoxConf) config;
            createBoxFixture(body, boxConf);
            
            bodies.put(body, new GameRenderer.RenderInfo(GameRenderer.RenderType.BOX, boxConf.getWidth(), boxConf.getHeight(), 0, getColorForSkin(boxConf.getSkinId()), boxConf.getSkinId()));
            
        } else if (config instanceof BucketConf) {
            BucketConf bucketConf = (BucketConf) config;
            createBucketFixture(body, bucketConf);
            
            bodies.put(body, new GameRenderer.RenderInfo(GameRenderer.RenderType.BUCKET, bucketConf.getWidth(), bucketConf.getHeight(), bucketConf.getThickness(), getColorForSkin(bucketConf.getSkinId()), bucketConf.getSkinId()));
            
        } else if (config instanceof GoalZoneConf) {
            GoalZoneConf goalConf = (GoalZoneConf) config;
            createGoalZoneFixture(body, goalConf);
            
            body.setUserData("goalzone");
            goalZones.add(body);
            
            bodies.put(body, new GameRenderer.RenderInfo(GameRenderer.RenderType.GOALZONE, goalConf.getWidth(), goalConf.getHeight(), 0, getColorForSkin(goalConf.getSkinId()), goalConf.getSkinId()));
        } else if (config instanceof RestrictionZoneConf) {
            RestrictionZoneConf restrictionConf = (RestrictionZoneConf) config;
            createRestrictionZoneFixture(body, restrictionConf);
            
            body.setUserData("restrictionzone");
            restrictionZones.add(body);
            
            bodies.put(body, new GameRenderer.RenderInfo(GameRenderer.RenderType.RESTRICTIONZONE, restrictionConf.getWidth(), restrictionConf.getHeight(), 0, getColorForSkin(restrictionConf.getSkinId()), restrictionConf.getSkinId()));
        }
    }
    
    /**
     * Erstellt ein Kreis-Fixture für einen Ball.
     * @param body Zielkörper
     * @param config Ball-Konfiguration
     */
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
    
    /**
     * Erstellt ein Rechteck-Fixture für eine Box.
     * @param body Zielkörper
     * @param config Box-Konfiguration
     */
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
    
    /**
     * Erstellt ein Bucket-Fixture (Boden und Wände).
     * @param body Zielkörper
     * @param config Bucket-Konfiguration
     */
    private void createBucketFixture(Body body, BucketConf config) {
        float width = config.getWidth();
        float height = config.getHeight();
        float thickness = config.getThickness();
        float wallAngle = config.getWallAngle();
        
        PolygonShape bottom = new PolygonShape();
        bottom.setAsBox(width / 2, thickness / 2, new Vec2(0, 0), 0);
        
        PolygonShape leftWall = new PolygonShape();
        leftWall.setAsBox(thickness / 2, height / 2, new Vec2(-width / 2 - thickness / 2, -height / 2), wallAngle);
        
        PolygonShape rightWall = new PolygonShape();
        rightWall.setAsBox(thickness / 2, height / 2, new Vec2(width / 2 + thickness / 2, -height / 2), -wallAngle);
        
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.3f;
        fixtureDef.restitution = 0.2f;
        
        fixtureDef.shape = bottom;
        body.createFixture(fixtureDef);
        
        fixtureDef.shape = leftWall;
        body.createFixture(fixtureDef);
        
        fixtureDef.shape = rightWall;
        body.createFixture(fixtureDef);
    }
    
    /**
     * Erstellt ein Fixture für die Zielzone.
     * @param body Zielkörper
     * @param config Zielzonen-Konfiguration
     */
    private void createGoalZoneFixture(Body body, GoalZoneConf config) {
        PolygonShape goalZone = new PolygonShape();
        goalZone.setAsBox(config.getWidth() / 2, config.getHeight() / 2);
        
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = goalZone;
        fixtureDef.isSensor = true;
        
        body.createFixture(fixtureDef);
    }
    
    /**
     * Erstellt ein Fixture für die Restriktionszone.
     * @param body Zielkörper
     * @param config Restriktionszonen-Konfiguration
     */
    private void createRestrictionZoneFixture(Body body, RestrictionZoneConf config) {
        PolygonShape restrictionZone = new PolygonShape();
        restrictionZone.setAsBox(config.getWidth() / 2, config.getHeight() / 2);
        
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = restrictionZone;
        fixtureDef.isSensor = true;
        
        body.createFixture(fixtureDef);
    }
    
    /**
     * Erstellt die Begrenzungswände der Welt.
     */
    public void createWorldBounds() {
        float canvasWidth = (float)(gameCanvas.getWidth() / SCALE);
        float canvasHeight = (float)(gameCanvas.getHeight() / SCALE);
        
        createWall(canvasWidth / 2, -0.1f, canvasWidth, 0.2f, false);
        createWall(canvasWidth / 2, canvasHeight + 0.1f, canvasWidth, 0.2f, true);
        createWall(-0.1f, canvasHeight / 2, 0.2f, canvasHeight, false);
        createWall(canvasWidth + 0.1f, canvasHeight / 2, 0.2f, canvasHeight, false);
    }
    
    /**
     * Erstellt eine Wand an der angegebenen Position.
     * @param x Mittelpunkt X
     * @param y Mittelpunkt Y
     * @param width Breite
     * @param height Höhe
     * @param isFloor true, wenn Boden
     */
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
            fixtureDef.friction = 0.05f;
            fixtureDef.restitution = 0.2f;
        } else {
            fixtureDef.friction = 0.3f;
            fixtureDef.restitution = 0.2f;
        }
        
        body.createFixture(fixtureDef);
    }
    
    /**
     * Prüft, ob der Gameball die Zielzone berührt und löst ggf. den Sieg aus.
     * @param contact Kontaktobjekt
     */
    private void checkGoalContact(Contact contact) {
        Body bodyA = contact.getFixtureA().getBody();
        Body bodyB = contact.getFixtureB().getBody();
        
        boolean gameballTouchesGoal = false;
        
        if (isGameBall(bodyA) && isGoalZone(bodyB)) {
            gameballTouchesGoal = true;
        } else if (isGameBall(bodyB) && isGoalZone(bodyA)) {
            gameballTouchesGoal = true;
        }
        
        if (gameballTouchesGoal && !gameWon) {
            gameWon = true;
            if (onGameWon != null) {
                Platform.runLater(onGameWon);
            }
        }
    }
    
    /**
     * Prüft, ob ein Body ein Gameball ist.
     * @param body Body
     * @return true, wenn Gameball
     */
    private boolean isGameBall(Body body) {
        Object userData = body.getUserData();
        return "gameball".equals(userData);
    }
    
    /**
     * Prüft, ob ein Body eine Zielzone ist.
     * @param body Body
     * @return true, wenn Zielzone
     */
    private boolean isGoalZone(Body body) {
        Object userData = body.getUserData();
        return "goalzone".equals(userData);
    }
    
    /**
     * Wendet Auftrieb und Luftwiderstand auf Ballons an.
     */
    public void applyBalloonBuoyancy() {
        for (Map.Entry<Body, GameRenderer.RenderInfo> entry : bodies.entrySet()) {
            Body body = entry.getKey();
            Object userData = body.getUserData();
            
            if ("balloon".equals(userData)) {
                Vec2 position = body.getPosition();
                Vec2 velocity = body.getLinearVelocity();
                
                // Erhöhte Auftriebskraft
                float buoyancyForce = 15.0f;
                Vec2 buoyancy = new Vec2(0, -buoyancyForce);
                
                // Horizontale Kraft für besseres Wegdrücken
                float horizontalForce = 8.0f;
                Vec2 horizontal = new Vec2(horizontalForce, 0);
                
                body.applyForce(buoyancy, position);
                body.applyForce(horizontal, position);
                
                // Reduzierter Luftwiderstand für mehr Kraft
                float dragForce = 0.99f;
                velocity.mulLocal(dragForce);
                body.setLinearVelocity(velocity);
            }
        }
    }
    
    /**
     * Gibt die Farbe für einen Skin zurück.
     * @param skinId Skin-ID
     * @return JavaFX-Farbe
     */
    private Color getColorForSkin(String skinId) {
        switch (skinId) {
            case "tennisball": return Color.LIME;
            case "bowlingball": return Color.web("#2C3E50");
            case "billiardball": return Color.web("#ECF0F1");
            case "balloon": return Color.web("#E91E63");
            case "log": return Color.web("#8B4513");
            case "plank": return Color.web("#D2691E");
            case "domino": return Color.web("#95A5A6");
            case "cratebox": return Color.web("#FF6347");
            case "bucket": return Color.web("#3498DB");
            case "gameball": return Color.ORANGE;
            case "goalzone": return Color.LIGHTGREEN.deriveColor(0, 1, 1, 0.7);
            case "restrictionzone": return Color.RED.deriveColor(0, 1, 1, 0.7);
            default: return Color.web("#7F8C8D");
        }
    }
} 
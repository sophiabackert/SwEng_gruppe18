package mm.objects.components;

import mm.objects.Component;
import mm.objects.GameObject;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import java.util.Objects;

/**
 * Komponente für die physikalische Simulation eines GameObjects.
 */
public class PhysicsComponent extends Component {
    private Body body;
    private World world;
    private BodyType bodyType = BodyType.DYNAMIC;
    private boolean isSensor = false;
    private FixtureDef fixtureDef;

    /**
     * @deprecated Verwende stattdessen den Konstruktor mit World-Parameter
     */
    @Deprecated
    public PhysicsComponent(GameObject gameObject) {
        super(gameObject);
        this.fixtureDef = new FixtureDef();
    }

    /**
     * Erstellt eine neue PhysicsComponent mit einer gültigen World-Referenz.
     * 
     * @param world Die physikalische Welt (darf nicht null sein)
     * @param gameObject Das zugehörige GameObject (darf nicht null sein)
     */
    public PhysicsComponent(World world, GameObject gameObject) {
        super(gameObject);
        this.world = Objects.requireNonNull(world, "World must not be null");
        this.fixtureDef = new FixtureDef();
    }

    /**
     * Gibt die FixtureDef zurück.
     */
    public FixtureDef getFixtureDef() {
        return fixtureDef;
    }

    /**
     * Erstellt einen kreisförmigen Körper.
     */
    public void createCircleBody(Vec2 position, float radius, BodyType type) {
        if (world == null) {
            throw new IllegalStateException("World must be set before creating a body");
        }

        // Körper-Definition erstellen
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = type;
        bodyDef.position.set(position);
        
        // Körper in der Welt erstellen
        body = world.createBody(bodyDef);
        
        // Kreisförmige Form erstellen
        CircleShape shape = new CircleShape();
        shape.setRadius(radius);
        
        // Form dem Körper mit den FixtureDef-Eigenschaften hinzufügen
        fixtureDef.shape = shape;
        body.createFixture(fixtureDef);
        
        // Position des GameObjects aktualisieren
        gameObject.setPosition(position.x, position.y);
    }

    /**
     * Erstellt einen rechteckigen Körper.
     */
    public void createBoxBody(float width, float height, float density, float restitution, float friction, BodyType bodyType) {
        Objects.requireNonNull(world, "World must be set before creating a body");
        
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = bodyType;
        bodyDef.position.set(gameObject.getPosition());
        
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2, height / 2);
        
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = density;
        fixtureDef.restitution = restitution;
        fixtureDef.friction = friction;
        
        body = world.createBody(bodyDef);
        body.createFixture(fixtureDef);
        body.setUserData(gameObject);
    }
    
    /**
     * Setzt die physikalische Welt.
     */
    public void setWorld(World world) {
        this.world = world;
    }
    
    /**
     * Gibt die physikalische Welt zurück.
     */
    public World getWorld() {
        return world;
    }
    
    /**
     * Gibt den physikalischen Körper zurück.
     */
    public Body getBody() {
        return body;
    }
    
    @Override
    public void update(float deltaTime) {
        // Position des GameObjects mit der Position des physikalischen Körpers synchronisieren
        if (body != null) {
            Vec2 pos = body.getPosition();
            gameObject.setPosition(pos.x, pos.y);
            gameObject.setRotation(body.getAngle());
        }
    }
    
    @Override
    public void reset() {
        if (body != null) {
            // Position und Geschwindigkeit zurücksetzen
            body.setLinearVelocity(new Vec2(0, 0));
            body.setAngularVelocity(0);
            body.setTransform(new Vec2(0, 0), 0);
        }
    }

    @Override
    public void onAttach() {
        // Get the physics world from the game engine
        // This would typically be set by the game engine when the component is created
    }

    @Override
    public void onDetach() {
        if (body != null && world != null) {
            world.destroyBody(body);
            body = null;
        }
    }

    // Getters and setters
    public float getDensity() {
        return body.getFixtureList().getDensity();
    }

    public float getFriction() {
        return body.getFixtureList().getFriction();
    }

    public float getRestitution() {
        return body.getFixtureList().getRestitution();
    }

    public boolean isSensor() {
        return isSensor;
    }

    /**
     * Erstellt einen rechteckigen Körper für die Physik-Simulation.
     *
     * @param position Position des Körpers
     * @param width Breite des Rechtecks
     * @param height Höhe des Rechtecks
     * @param type Körpertyp (statisch, dynamisch, kinematisch)
     */
    public void createRectangleBody(Vec2 position, float width, float height, BodyType type) {
        if (world == null) {
            throw new IllegalStateException("World must be set before creating a body");
        }

        // Körper-Definition erstellen
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = type;
        bodyDef.position.set(position);
        
        // Körper in der Welt erstellen
        body = world.createBody(bodyDef);
        
        // Rechteckige Form erstellen
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2, height / 2); // Halbbreite und -höhe verwenden
        
        // Form dem Körper hinzufügen
        body.createFixture(shape, 1.0f);
        
        // Position des GameObjects aktualisieren
        gameObject.setPosition(position.x, position.y);
    }

    /**
     * Erstellt einen kreisförmigen Körper für die Physik-Simulation.
     *
     * @param position Position des Körpers
     * @param radius Radius des Kreises
     * @param type Körpertyp (statisch, dynamisch, kinematisch)
     */
    public void createCircleBody(float radius, float density, float restitution, float friction, BodyType bodyType) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = bodyType;
        bodyDef.position.set(gameObject.getPosition());
        
        CircleShape shape = new CircleShape();
        shape.setRadius(radius);
        
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = density;
        fixtureDef.restitution = restitution;
        fixtureDef.friction = friction;
        
        body = world.createBody(bodyDef);
        body.createFixture(fixtureDef);
        body.setUserData(gameObject);
    }

    /**
     * Zerstört den aktuellen Körper in der Physik-Simulation.
     */
    public void destroyBody() {
        if (world != null && body != null) {
            world.destroyBody(body);
            body = null;
        }
    }
} 
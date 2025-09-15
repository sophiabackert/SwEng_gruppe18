package mm.objects.misc;

import mm.objects.GameObject;
import mm.objects.components.CollisionBehavior;
import mm.objects.components.PhysicsComponent;
import mm.objects.components.RenderComponent;
import mm.objects.containers.Log;
import mm.objects.containers.Plank;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.contacts.Contact;
import javafx.scene.paint.Color;

/**
 * Balloon implementation with very low density and high bounce.
 * Features upward force to simulate helium.
 */
public class Balloon extends GameObject {
    
    // Balloon specific constants
    private static final float DEFAULT_RADIUS = 0.15f; // 15cm radius
    private static final float DEFAULT_DENSITY = 0.1f; // Very light
    private static final float DEFAULT_RESTITUTION = 0.9f; // Very bouncy
    private static final float DEFAULT_FRICTION = 0.1f; // Low friction
    private static final float LIFT_FORCE = 2.0f; // Upward force in N/m²
    private static final float FLOAT_FORCE = 2.0f;
    private static final float POP_THRESHOLD = 5.0f;
    
    private final float radius;
    
    /**
     * Creates a balloon at the specified position with default properties.
     *
     * @param position Initial position of the balloon
     */
    public Balloon(Vec2 position) {
        this(position, DEFAULT_RADIUS, DEFAULT_DENSITY, DEFAULT_RESTITUTION, DEFAULT_FRICTION);
    }
    
    /**
     * Creates a balloon with custom properties.
     *
     * @param position Initial position
     * @param radius Radius in meters
     * @param density Mass density
     * @param restitution Bounciness
     * @param friction Friction coefficient
     */
    public Balloon(Vec2 position, float radius, float density, float restitution, float friction) {
        super(position);
        this.radius = radius;
        
        // Add physics component with circle shape
        PhysicsComponent physics = new PhysicsComponent(this);
        physics.createCircleBody(position, radius, BodyType.DYNAMIC);
        physics.getBody().getFixtureList().setDensity(density);
        physics.getBody().getFixtureList().setRestitution(restitution);
        physics.getBody().getFixtureList().setFriction(friction);
        physics.getBody().resetMassData();
        physics.getBody().setGravityScale(-0.5f); // Negativer Wert lässt den Ballon schweben
        addComponent(physics);
        
        // Add render component
        RenderComponent render = new RenderComponent(this);
        render.setFillColor(Color.RED);
        addComponent(render);
        
        // Kollisionsverhalten hinzufügen
        addComponent(new BalloonCollisionBehavior(this));
    }
    
    /**
     * Spezifisches Kollisionsverhalten für den Ballon.
     */
    private class BalloonCollisionBehavior extends CollisionBehavior {
        private static final float COLLISION_THRESHOLD = 5.0f;

        public BalloonCollisionBehavior(GameObject gameObject) {
            super(gameObject);
        }
        
        @Override
        public void onCollisionEnter(GameObject other, Contact contact) {
            // Prüfen, ob der Ballon mit einem scharfen Objekt kollidiert
            if (other instanceof Log || other instanceof Plank) {
                destroy();
            }
        }
        
        @Override
        public void onCollisionExit(GameObject other, Contact contact) {
            // Keine spezielle Aktion beim Verlassen der Kollision
        }
        
        @Override
        public void onCollisionStay(GameObject other, Contact contact) {
            float impulseMagnitude = contact.getManifold().points[0].normalImpulse;
            if (impulseMagnitude > getCollisionThreshold()) {
                destroy();
            }
        }

        @Override
        public float getCollisionThreshold() {
            return COLLISION_THRESHOLD;
        }

        @Override
        public void reset() {
            // Keine spezielle Reset-Logik erforderlich
        }

        @Override
        public void onAttach() {
            // Keine spezielle Initialisierung erforderlich
        }

        @Override
        public void onDetach() {
            // Keine spezielle Bereinigung erforderlich
        }
    }
    
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        
        // Konstante Aufwärtskraft anwenden, wenn der Ballon nicht zerstört ist
        PhysicsComponent physics = getComponent(PhysicsComponent.class);
        CollisionBehavior collision = getComponent(CollisionBehavior.class);
        
        if (physics != null && collision != null && !collision.isDestroyed()) {
            physics.getBody().applyForceToCenter(new Vec2(0, FLOAT_FORCE));
        }
    }
    
    public float getRadius() {
        return radius;
    }
}

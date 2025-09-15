package mm.objects.misc;

import mm.objects.GameObject;
import mm.objects.components.AudioComponent;
import mm.objects.components.CollisionBehavior;
import mm.objects.components.PhysicsComponent;
import mm.objects.components.RenderComponent;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.contacts.Contact;

/**
 * Repräsentiert einen Ball im Spiel.
 */
public class Ball extends GameObject {
    private static final float BALL_RADIUS = 0.3f;
    private static final float BALL_DENSITY = 1.0f;
    private static final float BALL_RESTITUTION = 0.7f;
    private static final float BALL_FRICTION = 0.3f;
    private static final float BOUNCE_THRESHOLD = 2.0f;
    
    public Ball(Vec2 position) {
        super(position);
        
        // Physik-Komponente hinzufügen
        PhysicsComponent physics = new PhysicsComponent(this);
        physics.createCircleBody(BALL_RADIUS, BALL_DENSITY, BALL_RESTITUTION, BALL_FRICTION, BodyType.DYNAMIC);
        addComponent(physics);
        
        // Render-Komponente hinzufügen
        RenderComponent render = new RenderComponent(this);
        // TODO: Ball-Textur laden
        addComponent(render);
        
        // Kollisionsverhalten hinzufügen
        addComponent(new BallCollisionBehavior(this));
    }
    
    /**
     * Spezifisches Kollisionsverhalten für den Ball.
     */
    private class BallCollisionBehavior extends CollisionBehavior {
        public BallCollisionBehavior(GameObject gameObject) {
            super(gameObject);
            setCollisionThreshold(BOUNCE_THRESHOLD);
        }
        
        @Override
        public void onCollisionEnter(GameObject other, Contact contact) {
            // Kollisionssound abspielen
            AudioComponent audio = gameObject.getComponent(AudioComponent.class);
            if (audio != null) {
                audio.playSound("bounce");
            }
        }
        
        @Override
        public void onCollisionExit(GameObject other, Contact contact) {
            // Keine spezielle Aktion beim Verlassen der Kollision
        }
        
        @Override
        public void onCollisionStay(GameObject other, Contact contact) {
            // Keine spezielle Aktion während der Kollision
        }
        
        @Override
        public void onCollisionSolved(GameObject other, ContactImpulse impulse, Contact contact) {
            float impulseMagnitude = impulse.normalImpulses[0];
            AudioComponent audio = gameObject.getComponent(AudioComponent.class);
            
            if (audio != null) {
                if (impulseMagnitude > getCollisionThreshold()) {
                    audio.playSound("bounce");
                } else {
                    audio.playSound("roll");
                }
            }
        }
    }
    
    @Override
    public void reset() {
        super.reset();
        // Spezifische Ball-Resets können hier hinzugefügt werden
    }
} 
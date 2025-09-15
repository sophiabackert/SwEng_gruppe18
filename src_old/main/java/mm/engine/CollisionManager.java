package mm.engine;

import mm.objects.GameObject;
import mm.objects.components.CollisionBehavior;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.dynamics.contacts.Contact;

/**
 * Verwaltet die Kollisionen zwischen GameObjects.
 * Implementiert das ContactListener-Interface von JBox2D.
 */
public class CollisionManager implements ContactListener {
    
    @Override
    public void beginContact(Contact contact) {
        GameObject objA = (GameObject) contact.getFixtureA().getBody().getUserData();
        GameObject objB = (GameObject) contact.getFixtureB().getBody().getUserData();
        
        if (objA != null && objB != null) {
            CollisionBehavior behaviorA = objA.getComponent(CollisionBehavior.class);
            CollisionBehavior behaviorB = objB.getComponent(CollisionBehavior.class);
            
            if (behaviorA != null) {
                behaviorA.onCollisionEnter(objB, contact);
            }
            
            if (behaviorB != null) {
                behaviorB.onCollisionEnter(objA, contact);
            }
        }
    }
    
    @Override
    public void endContact(Contact contact) {
        GameObject objA = (GameObject) contact.getFixtureA().getBody().getUserData();
        GameObject objB = (GameObject) contact.getFixtureB().getBody().getUserData();
        
        if (objA != null && objB != null) {
            CollisionBehavior behaviorA = objA.getComponent(CollisionBehavior.class);
            CollisionBehavior behaviorB = objB.getComponent(CollisionBehavior.class);
            
            if (behaviorA != null) {
                behaviorA.onCollisionExit(objB, contact);
            }
            
            if (behaviorB != null) {
                behaviorB.onCollisionExit(objA, contact);
            }
        }
    }
    
    @Override
    public void preSolve(Contact contact, org.jbox2d.collision.Manifold oldManifold) {
        // Hier können wir Kollisionen vor der Auflösung modifizieren
        // z.B. bestimmte Kollisionen deaktivieren
        GameObject objA = (GameObject) contact.getFixtureA().getBody().getUserData();
        GameObject objB = (GameObject) contact.getFixtureB().getBody().getUserData();
        
        // Prüfe, ob einer der Körper ein Sensor ist
        if (contact.getFixtureA().isSensor() || contact.getFixtureB().isSensor()) {
            // Sensoren lösen keine physikalische Kollision aus
            contact.setEnabled(false);
        }
    }
    
    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        GameObject objA = (GameObject) contact.getFixtureA().getBody().getUserData();
        GameObject objB = (GameObject) contact.getFixtureB().getBody().getUserData();
        
        if (objA != null && objB != null) {
            CollisionBehavior behaviorA = objA.getComponent(CollisionBehavior.class);
            CollisionBehavior behaviorB = objB.getComponent(CollisionBehavior.class);
            
            if (behaviorA != null) {
                behaviorA.onCollisionSolved(objB, impulse, contact);
            }
            
            if (behaviorB != null) {
                behaviorB.onCollisionSolved(objA, impulse, contact);
            }
        }
    }
} 
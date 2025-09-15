package mm.objects.misc;

import mm.objects.GameObject;
import mm.objects.components.CollisionBehavior;
import mm.objects.containers.Log;
import mm.objects.containers.Plank;
import org.jbox2d.dynamics.contacts.Contact;

/**
 * Kollisionsverhalten für Ballons.
 */
public class BalloonCollisionBehavior extends CollisionBehavior {
    private static final float COLLISION_THRESHOLD = 2.0f;

    public BalloonCollisionBehavior(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void onCollisionEnter(GameObject other, Contact contact) {
        if (other instanceof Log || other instanceof Plank) {
            gameObject.destroy();
        }
    }

    @Override
    public void onCollisionExit(GameObject other, Contact contact) {
        // Keine spezielle Aktion beim Verlassen der Kollision
    }

    @Override
    public void onCollisionStay(GameObject other, Contact contact) {
        float impulseMagnitude = contact.getManifold().points[0].normalImpulse;
        if (impulseMagnitude > COLLISION_THRESHOLD) {
            gameObject.destroy();
        }
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
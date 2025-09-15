package mm.objects.misc;

import mm.objects.GameObject;
import mm.objects.components.CollisionBehavior;
import org.jbox2d.dynamics.contacts.Contact;

/**
 * Kollisionsverhalten für Dominosteine.
 */
public class DominoCollisionBehavior extends CollisionBehavior {
    private static final float COLLISION_THRESHOLD = 2.0f;

    public DominoCollisionBehavior(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void onCollisionEnter(GameObject other, Contact contact) {
        // Keine spezielle Aktion beim Beginn der Kollision
    }

    @Override
    public void onCollisionExit(GameObject other, Contact contact) {
        // Keine spezielle Aktion beim Verlassen der Kollision
    }

    @Override
    public void onCollisionStay(GameObject other, Contact contact) {
        float impulseMagnitude = contact.getManifold().points[0].normalImpulse;
        if (impulseMagnitude > COLLISION_THRESHOLD) {
            // Optional: Spezielle Effekte oder Aktionen bei starker Kollision
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
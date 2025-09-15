package mm.objects.components;

import mm.objects.Component;
import mm.objects.GameObject;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.dynamics.contacts.Contact;

/**
 * Basisklasse für Kollisionsverhalten.
 */
public abstract class CollisionBehavior extends Component {
    private float collisionThreshold = 1.0f;
    private boolean isDestroyed = false;

    public CollisionBehavior(GameObject gameObject) {
        super(gameObject);
    }

    /**
     * Wird aufgerufen, wenn eine Kollision beginnt.
     * @param other Das andere GameObject
     * @param contact Die Kollisionsinformationen
     */
    public abstract void onCollisionEnter(GameObject other, Contact contact);

    /**
     * Wird aufgerufen, wenn eine Kollision endet.
     * @param other Das andere GameObject
     * @param contact Die Kollisionsinformationen
     */
    public abstract void onCollisionExit(GameObject other, Contact contact);

    /**
     * Wird aufgerufen, während eine Kollision besteht.
     * @param other Das andere GameObject
     * @param contact Die Kollisionsinformationen
     */
    public abstract void onCollisionStay(GameObject other, Contact contact);

    /**
     * Wird aufgerufen, wenn eine Kollision aufgelöst wurde.
     * @param other Das andere GameObject
     * @param impulse Die Aufprallstärke
     * @param contact Die Kollisionsinformationen
     */
    public void onCollisionSolved(GameObject other, ContactImpulse impulse, Contact contact) {
        // Standardimplementierung - kann von Unterklassen überschrieben werden
    }

    /**
     * Setzt den Schwellenwert für Kollisionsreaktionen.
     * @param threshold Der neue Schwellenwert
     */
    public void setCollisionThreshold(float threshold) {
        this.collisionThreshold = threshold;
    }

    /**
     * Gibt den aktuellen Kollisionsschwellenwert zurück.
     * @return Der Schwellenwert
     */
    public float getCollisionThreshold() {
        return collisionThreshold;
    }

    /**
     * Markiert das GameObject als zerstört.
     */
    protected void destroy() {
        if (!isDestroyed) {
            isDestroyed = true;
            // Optional: Zerstörungsanimation oder Soundeffekt abspielen
            AudioComponent audio = gameObject.getComponent(AudioComponent.class);
            if (audio != null) {
                audio.playSound("destroy");
            }
            // GameObject aus der Welt entfernen
            gameObject.getComponent(PhysicsComponent.class).getWorld().destroyBody(
                gameObject.getComponent(PhysicsComponent.class).getBody());
        }
    }

    /**
     * Prüft, ob das GameObject zerstört wurde.
     * @return true wenn das GameObject zerstört wurde
     */
    public boolean isDestroyed() {
        return isDestroyed;
    }

    @Override
    public void update(float deltaTime) {
        // Keine Update-Logik erforderlich
    }

    @Override
    public void reset() {
        // Keine Reset-Logik erforderlich
    }

    @Override
    public void onAttach() {
        // Keine Initialisierung erforderlich
    }

    @Override
    public void onDetach() {
        // Keine Bereinigung erforderlich
    }
} 
package mm.objects.components;

import mm.objects.Component;
import mm.objects.GameObject;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.dynamics.contacts.Contact;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Komponente für die Kollisionserkennung eines GameObjects.
 */
public class CollisionComponent extends Component implements ContactListener {
    private static final float COLLISION_THRESHOLD = 1.0f; // Minimale Aufprallstärke für Soundeffekte
    
    // Callback-Listen für verschiedene Kollisionsereignisse
    private final List<Consumer<GameObject>> onCollisionEnterCallbacks;
    private final List<Consumer<GameObject>> onCollisionExitCallbacks;
    private final List<Consumer<GameObject>> onCollisionStayCallbacks;
    
    // Aktuell kollidierende Objekte
    private final List<GameObject> currentCollisions;
    
    // Kollisionsfilter
    private short categoryBits = 0x0001;  // Standard-Kategorie
    private short maskBits = -1;          // Kollidiert mit allem
    
    private CollisionBehavior behavior;

    /**
     * Erstellt eine neue CollisionComponent.
     */
    public CollisionComponent(GameObject gameObject) {
        super(gameObject);
        this.onCollisionEnterCallbacks = new ArrayList<>();
        this.onCollisionExitCallbacks = new ArrayList<>();
        this.onCollisionStayCallbacks = new ArrayList<>();
        this.currentCollisions = new ArrayList<>();
    }
    
    @Override
    protected void onAttach() {
        PhysicsComponent physics = getGameObject().getComponent(PhysicsComponent.class);
        if (physics != null) {
            // Setze die Kollisionsfilter für alle Fixtures
            physics.getFixtureDef().filter.categoryBits = categoryBits;
            physics.getFixtureDef().filter.maskBits = maskBits;
        }
    }
    
    @Override
    protected void onDetach() {
        currentCollisions.clear();
    }
    
    @Override
    public void update(float deltaTime) {
        // Keine Update-Logik erforderlich
    }
    
    /**
     * Setzt die Kollisionskategorie dieses Objekts.
     * @param category Die Kategorie als Bit-Maske
     */
    public void setCategory(short category) {
        this.categoryBits = category;
        updateFilters();
    }
    
    /**
     * Setzt die Kollisionsmaske dieses Objekts.
     * @param mask Die Maske als Bit-Maske
     */
    public void setCollisionMask(short mask) {
        this.maskBits = mask;
        updateFilters();
    }
    
    /**
     * Fügt einen Callback für Kollisionsbeginn hinzu.
     * @param callback Der auszuführende Callback
     */
    public void addOnCollisionEnter(Consumer<GameObject> callback) {
        onCollisionEnterCallbacks.add(callback);
    }
    
    /**
     * Fügt einen Callback für Kollisionsende hinzu.
     * @param callback Der auszuführende Callback
     */
    public void addOnCollisionExit(Consumer<GameObject> callback) {
        onCollisionExitCallbacks.add(callback);
    }
    
    /**
     * Fügt einen Callback für anhaltende Kollisionen hinzu.
     * @param callback Der auszuführende Callback
     */
    public void addOnCollisionStay(Consumer<GameObject> callback) {
        onCollisionStayCallbacks.add(callback);
    }
    
    /**
     * Setzt das Kollisionsverhalten.
     */
    public void setBehavior(CollisionBehavior behavior) {
        this.behavior = behavior;
    }
    
    /**
     * Gibt das Kollisionsverhalten zurück.
     */
    public CollisionBehavior getBehavior() {
        return behavior;
    }
    
    /**
     * Wird aufgerufen, wenn eine Kollision beginnt.
     */
    public void onCollisionEnter(GameObject other, Contact contact) {
        if (behavior != null) {
            behavior.onCollisionEnter(other, contact);
        }
    }
    
    /**
     * Wird aufgerufen, wenn eine Kollision endet.
     */
    public void onCollisionExit(GameObject other, Contact contact) {
        if (behavior != null) {
            behavior.onCollisionExit(other, contact);
        }
    }
    
    /**
     * Wird aufgerufen, während eine Kollision besteht.
     */
    public void onCollisionStay(GameObject other, Contact contact) {
        if (behavior != null) {
            behavior.onCollisionStay(other, contact);
        }
    }
    
    // Private Hilfsmethoden
    
    private void updateFilters() {
        PhysicsComponent physics = getGameObject().getComponent(PhysicsComponent.class);
        if (physics != null && physics.getBody() != null) {
            physics.getFixtureDef().filter.categoryBits = categoryBits;
            physics.getFixtureDef().filter.maskBits = maskBits;
        }
    }
    
    private void notifyCollisionEnter(GameObject other) {
        for (Consumer<GameObject> callback : onCollisionEnterCallbacks) {
            callback.accept(other);
        }
    }
    
    private void notifyCollisionExit(GameObject other) {
        for (Consumer<GameObject> callback : onCollisionExitCallbacks) {
            callback.accept(other);
        }
    }
    
    private void notifyCollisionStay(GameObject other) {
        for (Consumer<GameObject> callback : onCollisionStayCallbacks) {
            callback.accept(other);
        }
    }

    @Override
    public void beginContact(Contact contact) {
        // Kollisionsbeginn
        GameObject objectA = (GameObject) contact.getFixtureA().getBody().getUserData();
        GameObject objectB = (GameObject) contact.getFixtureB().getBody().getUserData();
        
        handleCollisionStart(objectA, objectB);
    }

    @Override
    public void endContact(Contact contact) {
        // Kollisionsende
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        // Vor der Kollisionsauflösung
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        // Nach der Kollisionsauflösung
        float maxImpulse = 0;
        for (float imp : impulse.normalImpulses) {
            maxImpulse = Math.max(maxImpulse, imp);
        }
        
        if (maxImpulse > COLLISION_THRESHOLD) {
            GameObject objectA = (GameObject) contact.getFixtureA().getBody().getUserData();
            GameObject objectB = (GameObject) contact.getFixtureB().getBody().getUserData();
            
            handleCollisionImpact(objectA, objectB, maxImpulse);
        }
    }

    private void handleCollisionStart(GameObject objectA, GameObject objectB) {
        // Soundeffekte für spezielle Kollisionen (z.B. Domino kippt um)
        AudioComponent audioA = objectA.getComponent(AudioComponent.class);
        AudioComponent audioB = objectB.getComponent(AudioComponent.class);
        
        if (audioA != null) {
            audioA.playSound("collision_start");
        }
        if (audioB != null) {
            audioB.playSound("collision_start");
        }
    }

    private void handleCollisionImpact(GameObject objectA, GameObject objectB, float impulse) {
        // Soundeffekte basierend auf der Aufprallstärke
        AudioComponent audioA = objectA.getComponent(AudioComponent.class);
        AudioComponent audioB = objectB.getComponent(AudioComponent.class);
        
        if (audioA != null) {
            if (impulse > COLLISION_THRESHOLD * 2) {
                audioA.playSound("collision_hard");
            } else {
                audioA.playSound("collision_soft");
            }
        }
        
        if (audioB != null) {
            if (impulse > COLLISION_THRESHOLD * 2) {
                audioB.playSound("collision_hard");
            } else {
                audioB.playSound("collision_soft");
            }
        }
    }

    @Override
    public void reset() {
        // Keine Reset-Logik erforderlich
    }
} 
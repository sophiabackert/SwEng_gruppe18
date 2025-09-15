package mm.objects.misc;

import mm.objects.GameObject;
import mm.objects.components.CollisionBehavior;
import mm.objects.components.PhysicsComponent;
import mm.objects.components.RenderComponent;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.contacts.Contact;
import javafx.scene.paint.Color;

/**
 * Ein Domino-Objekt im Spiel.
 */
public class Domino extends GameObject {
    private static final float WIDTH = 0.1f;
    private static final float HEIGHT = 0.2f;
    private static final float DENSITY = 1.0f;
    private static final float FRICTION = 0.5f;
    private static final float RESTITUTION = 0.2f;
    private static final Color COLOR = Color.WHITE;

    /**
     * Erstellt einen neuen Domino.
     * @param position Die Startposition des Dominos
     */
    public Domino(Vec2 position) {
        super(position);
        initComponents();
    }

    private void initComponents() {
        // Physik-Komponente
        PhysicsComponent physics = new PhysicsComponent(this);
        physics.createBoxBody(WIDTH, HEIGHT, DENSITY, FRICTION, RESTITUTION, BodyType.DYNAMIC);
        addComponent(physics);

        // Render-Komponente
        RenderComponent render = new RenderComponent(this);
        render.setFillColor(COLOR);
        addComponent(render);

        // Kollisionsverhalten hinzufügen
        addComponent(new DominoCollisionBehavior(this));
    }

    /**
     * Kollisionsverhalten für den Domino.
     */
    private class DominoCollisionBehavior extends CollisionBehavior {
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
}
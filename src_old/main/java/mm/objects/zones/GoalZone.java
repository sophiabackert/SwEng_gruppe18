package mm.objects.zones;

import javafx.scene.paint.Color;
import mm.objects.GameObject;
import mm.objects.components.CollisionBehavior;
import mm.objects.components.PhysicsComponent;
import mm.objects.components.RenderComponent;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;

import java.util.Objects;

/**
 * Eine Zielzone für das Spiel.
 */
public class GoalZone extends GameObject {
    private static final float DEFAULT_WIDTH = 1.0f;
    private static final float DEFAULT_HEIGHT = 1.0f;
    private static final Color DEFAULT_COLOR = Color.GREEN.deriveColor(0, 1, 1, 0.3);

    private final float width;
    private final float height;
    private boolean isActive;
    private final World world;

    /**
     * Erstellt eine neue Zielzone mit Standardwerten.
     * @param world Die physikalische Welt (darf nicht null sein)
     * @param position Die Position der Zielzone
     */
    public GoalZone(World world, Vec2 position) {
        this(world, position, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Erstellt eine neue Zielzone mit angegebener Größe.
     * @param world Die physikalische Welt (darf nicht null sein)
     * @param position Die Position der Zielzone
     * @param width Die Breite der Zielzone
     * @param height Die Höhe der Zielzone
     */
    public GoalZone(World world, Vec2 position, float width, float height) {
        super(position);
        this.world = Objects.requireNonNull(world, "World must not be null");
        this.width = width;
        this.height = height;
        this.isActive = true;
        initComponents();
    }

    /**
     * Erstellt eine neue Zielzone mit angegebener Größe und Callback.
     * @param world Die physikalische Welt (darf nicht null sein)
     * @param position Die Position der Zielzone
     * @param width Die Breite der Zielzone
     * @param height Die Höhe der Zielzone
     * @param onObjectAttached Callback, der aufgerufen wird, wenn ein Objekt die Zone erreicht
     */
    public GoalZone(World world, Vec2 position, float width, float height, Runnable onObjectAttached) {
        super(position);
        this.world = Objects.requireNonNull(world, "World must not be null");
        this.width = width;
        this.height = height;
        this.isActive = true;
        initComponents(onObjectAttached);
    }

    private void initComponents() {
        initComponents(null);
    }

    private void initComponents(Runnable onObjectAttached) {
        // Physik-Komponente mit World-Referenz erstellen
        PhysicsComponent physics = new PhysicsComponent(world, this);
        physics.createBoxBody(width, height, 0.0f, 0.0f, 0.0f, BodyType.STATIC);
        addComponent(physics);

        // Render-Komponente
        RenderComponent render = new RenderComponent(this);
        render.setFillColor(DEFAULT_COLOR);
        addComponent(render);

        // Kollisionsverhalten hinzufügen
        addComponent(new GoalZoneCollisionBehavior(this, onObjectAttached));
    }

    /**
     * Gibt zurück, ob die Zielzone aktiv ist.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Setzt den Aktivitätsstatus der Zielzone.
     */
    public void setActive(boolean active) {
        this.isActive = active;
    }

    /**
     * Gibt die Breite der Zielzone zurück.
     */
    public float getWidth() {
        return width;
    }

    /**
     * Gibt die Höhe der Zielzone zurück.
     */
    public float getHeight() {
        return height;
    }

    /**
     * Prüft, ob ein bestimmtes GameObject sich in der Zielzone befindet.
     * @param obj Das zu prüfende GameObject
     * @return true wenn sich das Objekt in der Zone befindet
     */
    public boolean containsObject(GameObject obj) {
        if (obj == null) return false;
        
        PhysicsComponent physics = obj.getComponent(PhysicsComponent.class);
        if (physics == null || physics.getBody() == null) return false;
        
        Vec2 objPos = physics.getBody().getPosition();
        Vec2 zonePos = getPosition();
        
        return objPos.x >= zonePos.x - width/2 &&
               objPos.x <= zonePos.x + width/2 &&
               objPos.y >= zonePos.y - height/2 &&
               objPos.y <= zonePos.y + height/2;
    }

    /**
     * Kollisionsverhalten für die Zielzone.
     */
    private class GoalZoneCollisionBehavior extends CollisionBehavior {
        public GoalZoneCollisionBehavior(GameObject gameObject, Runnable onObjectAttached) {
            super(gameObject);
            this.onObjectAttached = onObjectAttached;
        }

        private final Runnable onObjectAttached;

        @Override
        public void onCollisionEnter(GameObject other, Contact contact) {
            if (isActive) {
                // Hier kann die Logik für das Erreichen des Ziels implementiert werden
                System.out.println("Ziel erreicht!");
                if (onObjectAttached != null) {
                    onObjectAttached.run();
                }
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
package mm.world;

import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import mm.objects.GameObject;
import org.jbox2d.common.Vec2;

import java.util.*;

/**
 * Repräsentiert die Spielwelt und verwaltet alle Spielobjekte.
 */
public class World {
    private static final Vec2 GRAVITY = new Vec2(0, -9.81f);
    
    private final org.jbox2d.dynamics.World physicsWorld;
    private final List<GameObject> gameObjects;
    private Vec2 gravity;
    
    // Weltgrenzen
    private final BoundingBox worldBounds;
    
    // Räumliche Indizierung (optional für spätere Optimierung)
    private final Map<String, List<GameObject>> spatialIndex;
    
    /**
     * Erstellt eine neue Spielwelt mit Standard-Gravitation.
     */
    public World() {
        this(GRAVITY);
    }
    
    /**
     * Erstellt eine neue Spielwelt mit angegebener Gravitation.
     * @param gravity Die Gravitation der Welt
     */
    public World(Vec2 gravity) {
        this.gravity = gravity;
        this.physicsWorld = new org.jbox2d.dynamics.World(gravity);
        this.gameObjects = new ArrayList<>();
        this.worldBounds = new BoundingBox(0, 0, 0, 0);
        this.spatialIndex = new HashMap<>();
    }
    
    /**
     * Setzt die Gravitation der Welt.
     * @param gravity Die neue Gravitation in m/s²
     */
    public void setGravity(double gravity) {
        this.gravity = new Vec2(0, (float) gravity);
        this.physicsWorld.setGravity(this.gravity);
    }
    
    /**
     * Gibt die aktuelle Gravitation der Welt zurück.
     * @return Die Gravitation in m/s²
     */
    public double getGravity() {
        return this.gravity.y;
    }
    
    /**
     * Löscht alle Objekte aus der Welt.
     */
    public void clear() {
        for (GameObject obj : new ArrayList<>(gameObjects)) {
            removeGameObject(obj);
        }
        gameObjects.clear();
    }
    
    /**
     * Fügt ein GameObject zur Welt hinzu.
     * @param object Das hinzuzufügende GameObject
     */
    public void addGameObject(GameObject object) {
        if (!gameObjects.contains(object)) {
            gameObjects.add(object);
        }
    }
    
    /**
     * Entfernt ein GameObject aus der Welt.
     * @param object Das zu entfernende GameObject
     */
    public void removeGameObject(GameObject object) {
        gameObjects.remove(object);
    }
    
    /**
     * Aktualisiert alle Objekte in der Welt.
     * @param deltaTime Die vergangene Zeit seit dem letzten Update in Sekunden
     */
    public void update(float deltaTime) {
        for (GameObject obj : gameObjects) {
            obj.update(deltaTime);
        }
    }
    
    /**
     * Rendert alle Objekte in der Welt.
     * @param gc Der GraphicsContext zum Rendern
     */
    public void render(GraphicsContext gc) {
        for (GameObject obj : gameObjects) {
            obj.render(gc);
        }
    }
    
    /**
     * Gibt die Liste aller GameObjects zurück.
     * @return Die Liste der GameObjects
     */
    public List<GameObject> getGameObjects() {
        return new ArrayList<>(gameObjects);
    }
    
    /**
     * Gibt alle Objekte in der Welt zurück.
     * @return Eine unveränderliche Liste aller Objekte
     */
    public List<GameObject> getObjects() {
        return Collections.unmodifiableList(gameObjects);
    }
    
    /**
     * Findet alle Objekte in einem bestimmten Bereich.
     * @param x X-Koordinate des Mittelpunkts
     * @param y Y-Koordinate des Mittelpunkts
     * @param radius Radius des Suchbereichs
     * @return Liste der gefundenen Objekte
     */
    public List<GameObject> findObjectsInRange(double x, double y, double radius) {
        List<GameObject> result = new ArrayList<>();
        double radiusSquared = radius * radius;
        
        for (GameObject obj : gameObjects) {
            Vec2 pos = obj.getBody().getPosition();
            double dx = pos.x - x;
            double dy = pos.y - y;
            if (dx * dx + dy * dy <= radiusSquared) {
                result.add(obj);
            }
        }
        
        return result;
    }
    
    /**
     * Prüft, ob eine Position innerhalb der Weltgrenzen liegt.
     * @param x X-Koordinate
     * @param y Y-Koordinate
     * @return true wenn die Position innerhalb der Grenzen liegt
     */
    public boolean isPositionInBounds(double x, double y) {
        return worldBounds.contains(x, y);
    }
    
    /**
     * Gibt die Grenzen der Welt zurück.
     * @return Die Weltgrenzen als BoundingBox
     */
    public BoundingBox getWorldBounds() {
        return worldBounds;
    }
    
    /**
     * Aktualisiert die räumlichen Indizes aller Objekte.
     * Dies ist eine Vorbereitungsmethode für zukünftige Optimierungen.
     */
    public void updateSpatialIndices() {
        // Hier können später Optimierungen implementiert werden
        // z.B. Quadtree oder Grid-basierte Indizierung
    }
    
    /**
     * Gibt die JBox2D-Physikwelt zurück.
     * @return Die JBox2D World-Instanz
     */
    public org.jbox2d.dynamics.World getPhysicsWorld() {
        return physicsWorld;
    }
} 
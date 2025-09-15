package mm.objects.pool;

import mm.objects.GameObject;
import org.jbox2d.common.Vec2;
import java.util.Queue;
import java.util.LinkedList;
import java.util.function.Function;

/**
 * Eine generische Objektpool-Implementierung für GameObjects.
 * @param <T> Der Typ des GameObject, das gepoolt werden soll
 */
public class ObjectPool<T extends GameObject> {
    private final Queue<T> pool;
    private final Function<Vec2, T> factory;
    private final int maxSize;
    
    /**
     * Erstellt einen neuen ObjectPool.
     * @param factory Eine Funktion, die neue Objekte erstellt
     * @param initialSize Die anfängliche Größe des Pools
     * @param maxSize Die maximale Größe des Pools
     */
    public ObjectPool(Function<Vec2, T> factory, int initialSize, int maxSize) {
        this.factory = factory;
        this.maxSize = maxSize;
        this.pool = new LinkedList<>();
        
        // Pool mit initialen Objekten füllen
        for (int i = 0; i < initialSize; i++) {
            T obj = factory.apply(new Vec2(0, 0));
            obj.setActive(false);
            pool.offer(obj);
        }
    }
    
    /**
     * Holt ein Objekt aus dem Pool.
     * @param position Die Position, an der das Objekt platziert werden soll
     * @return Ein Objekt aus dem Pool oder ein neues Objekt
     */
    public T acquire(Vec2 position) {
        T obj = pool.poll();
        if (obj == null) {
            // Pool ist leer, erstelle ein neues Objekt
            obj = factory.apply(position);
        } else {
            // Existierendes Objekt wiederverwenden
            obj.setPosition(position.x, position.y);
            obj.setActive(true);
            obj.reset(); // Optional: Objekt zurücksetzen
        }
        return obj;
    }
    
    /**
     * Gibt ein Objekt in den Pool zurück.
     * @param obj Das zurückzugebende Objekt
     */
    public void release(T obj) {
        if (obj != null && pool.size() < maxSize) {
            obj.setActive(false);
            pool.offer(obj);
        }
    }
    
    /**
     * Gibt die aktuelle Größe des Pools zurück.
     * @return Die Anzahl der Objekte im Pool
     */
    public int getSize() {
        return pool.size();
    }
    
    /**
     * Gibt die maximale Größe des Pools zurück.
     * @return Die maximale Anzahl der Objekte im Pool
     */
    public int getMaxSize() {
        return maxSize;
    }
    
    /**
     * Leert den Pool und gibt alle Ressourcen frei.
     */
    public void clear() {
        pool.clear();
    }
} 
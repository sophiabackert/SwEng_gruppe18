package mm.objects.pool;

import mm.objects.GameObject;
import mm.objects.misc.Ball;
import mm.objects.misc.Balloon;
import mm.objects.misc.Domino;
import org.jbox2d.common.Vec2;
import java.util.HashMap;
import java.util.Map;

/**
 * Verwaltet die verschiedenen ObjectPools für häufig verwendete GameObjects.
 */
public class ObjectPoolManager {
    private static final int DEFAULT_INITIAL_SIZE = 10;
    private static final int DEFAULT_MAX_SIZE = 50;
    
    private final Map<Class<? extends GameObject>, ObjectPool<?>> pools;
    
    private static ObjectPoolManager instance;
    
    private ObjectPoolManager() {
        pools = new HashMap<>();
        initializePools();
    }
    
    /**
     * Gibt die Singleton-Instanz des ObjectPoolManagers zurück.
     */
    public static ObjectPoolManager getInstance() {
        if (instance == null) {
            instance = new ObjectPoolManager();
        }
        return instance;
    }
    
    /**
     * Initialisiert die Standard-Pools.
     */
    private void initializePools() {
        // Pool für Bälle
        createPool(Ball.class, pos -> new Ball(pos), DEFAULT_INITIAL_SIZE, DEFAULT_MAX_SIZE);
        
        // Pool für Ballons
        createPool(Balloon.class, pos -> new Balloon(pos), DEFAULT_INITIAL_SIZE, DEFAULT_MAX_SIZE);
        
        // Pool für Dominosteine
        createPool(Domino.class, pos -> new Domino(pos), DEFAULT_INITIAL_SIZE, DEFAULT_MAX_SIZE);
    }
    
    /**
     * Erstellt einen neuen Pool für einen bestimmten GameObject-Typ.
     */
    public <T extends GameObject> void createPool(Class<T> type, java.util.function.Function<Vec2, T> factory,
                                                int initialSize, int maxSize) {
        pools.put(type, new ObjectPool<>(factory, initialSize, maxSize));
    }
    
    /**
     * Holt ein Objekt aus dem entsprechenden Pool.
     */
    @SuppressWarnings("unchecked")
    public <T extends GameObject> T acquire(Class<T> type, Vec2 position) {
        ObjectPool<T> pool = (ObjectPool<T>) pools.get(type);
        if (pool == null) {
            throw new IllegalArgumentException("Kein Pool für den Typ " + type.getSimpleName() + " gefunden");
        }
        return pool.acquire(position);
    }
    
    /**
     * Gibt ein Objekt in seinen Pool zurück.
     */
    @SuppressWarnings("unchecked")
    public <T extends GameObject> void release(T obj) {
        if (obj == null) return;
        
        ObjectPool<T> pool = (ObjectPool<T>) pools.get(obj.getClass());
        if (pool != null) {
            pool.release(obj);
        }
    }
    
    /**
     * Leert alle Pools.
     */
    public void clearAllPools() {
        pools.values().forEach(ObjectPool::clear);
    }
    
    /**
     * Gibt die Gesamtanzahl der Objekte in allen Pools zurück.
     */
    public int getTotalPoolSize() {
        return pools.values().stream()
            .mapToInt(ObjectPool::getSize)
            .sum();
    }
} 
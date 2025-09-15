package mm.inventory;

import java.util.*;

/**
 * Verwaltet das Inventar des Spiels.
 */
public class InventoryManager {
    private static InventoryManager instance;
    private final List<InventoryItem> items;
    private final Map<String, Integer> itemCounts;
    
    private InventoryManager() {
        items = new ArrayList<>();
        itemCounts = new HashMap<>();
    }
    
    public static InventoryManager getInstance() {
        if (instance == null) {
            instance = new InventoryManager();
        }
        return instance;
    }
    
    /**
     * Fügt ein Item zum Inventar hinzu.
     * @param type Der Typ des Items
     */
    public void addItem(String type) {
        // Suche nach einem vorhandenen Item des gleichen Typs
        for (InventoryItem item : items) {
            if (item.getType().equals(type)) {
                item.incrementCount();
                return;
            }
        }
        
        // Wenn kein vorhandenes Item gefunden wurde, erstelle ein neues
        items.add(new InventoryItem(type));
        updateItemCounts();
    }
    
    /**
     * Entfernt ein Item aus dem Inventar.
     * @param type Der Typ des Items
     * @return true wenn das Item erfolgreich entfernt wurde
     */
    public boolean removeItem(String type) {
        for (InventoryItem item : items) {
            if (item.getType().equals(type)) {
                if (item.getCount() > 1) {
                    item.decrementCount();
                } else {
                    items.remove(item);
                }
                updateItemCounts();
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gibt die Liste aller Items im Inventar zurück.
     * @return Die Liste der Items
     */
    public List<InventoryItem> getItems() {
        return Collections.unmodifiableList(items);
    }
    
    /**
     * Prüft, ob ein bestimmtes Item im Inventar verfügbar ist.
     * @param type Der Typ des Items
     * @return true wenn das Item verfügbar ist
     */
    public boolean hasItem(String type) {
        for (InventoryItem item : items) {
            if (item.getType().equals(type) && item.getCount() > 0) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gibt die Anzahl eines bestimmten Items im Inventar zurück.
     * @param type Der Typ des Items
     * @return Die Anzahl des Items
     */
    public int getItemCount(String type) {
        return itemCounts.getOrDefault(type, 0);
    }
    
    /**
     * Leert das Inventar vollständig.
     */
    public void clearInventory() {
        items.clear();
        itemCounts.clear();
    }
    
    /**
     * Aktualisiert die Item-Counts basierend auf den Items.
     */
    private void updateItemCounts() {
        itemCounts.clear();
        for (InventoryItem item : items) {
            itemCounts.put(item.getType(), item.getCount());
        }
    }
} 
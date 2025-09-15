package mm.inventory;

/**
 * Repräsentiert ein Item im Inventar.
 */
public class InventoryItem {
    private final String type;
    private int count;
    
    /**
     * Erstellt ein neues InventoryItem.
     * @param type Der Typ des Items
     */
    public InventoryItem(String type) {
        this(type, 1);
    }
    
    /**
     * Erstellt ein neues InventoryItem mit einer bestimmten Anzahl.
     * @param type Der Typ des Items
     * @param count Die Anzahl des Items
     */
    public InventoryItem(String type, int count) {
        this.type = type;
        this.count = count;
    }
    
    /**
     * Gibt den Typ des Items zurück.
     * @return Der Typ des Items
     */
    public String getType() {
        return type;
    }
    
    /**
     * Gibt die Anzahl des Items zurück.
     * @return Die Anzahl des Items
     */
    public int getCount() {
        return count;
    }
    
    /**
     * Setzt die Anzahl des Items.
     * @param count Die neue Anzahl
     */
    public void setCount(int count) {
        this.count = count;
    }
    
    /**
     * Erhöht die Anzahl des Items um 1.
     */
    public void incrementCount() {
        count++;
    }
    
    /**
     * Verringert die Anzahl des Items um 1.
     */
    public void decrementCount() {
        if (count > 0) {
            count--;
        }
    }
    
    public String getDisplayName() {
        switch (type) {
            case "ball":
                return "Ball";
            case "box":
                return "Box";
            case "plank":
                return "Plank";
            case "bucket":
                return "Bucket";
            default:
                return type;
        }
    }
    
    @Override
    public String toString() {
        return getDisplayName() + " (" + count + ")";
    }
} 
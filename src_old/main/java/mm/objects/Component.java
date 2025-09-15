package mm.objects;

/**
 * Basisklasse für alle Komponenten eines GameObjects.
 */
public abstract class Component {
    protected GameObject gameObject;
    private boolean enabled = true;

    /**
     * Erstellt eine neue Komponente für ein GameObject.
     * @param gameObject Das GameObject, zu dem diese Komponente gehört
     */
    protected Component(GameObject gameObject) {
        this.gameObject = gameObject;
    }

    /**
     * Aktualisiert die Komponente.
     * @param deltaTime Die vergangene Zeit seit dem letzten Update in Sekunden
     */
    public abstract void update(float deltaTime);

    /**
     * Wird aufgerufen, wenn die Komponente einem GameObject hinzugefügt wird.
     */
    protected abstract void onAttach();

    /**
     * Wird aufgerufen, wenn die Komponente von einem GameObject entfernt wird.
     */
    protected abstract void onDetach();

    /**
     * Setzt die Komponente auf ihren Ausgangszustand zurück.
     */
    public abstract void reset();

    /**
     * Aktiviert oder deaktiviert die Komponente.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gibt zurück, ob die Komponente aktiviert ist.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gibt das GameObject zurück, zu dem diese Komponente gehört.
     */
    public GameObject getGameObject() {
        return gameObject;
    }

    /**
     * Setzt das GameObject, zu dem diese Komponente gehört.
     */
    protected void setGameObject(GameObject gameObject) {
        this.gameObject = gameObject;
    }
} 
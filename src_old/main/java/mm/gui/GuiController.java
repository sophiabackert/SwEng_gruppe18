package mm.gui;

/**
 * Basisklasse für alle GUI-Controller.
 */
public abstract class GuiController {
    private ViewManager viewManager;
    
    /**
     * Setzt den ViewManager für diesen Controller.
     * @param viewManager Die ViewManager-Instanz
     */
    public void setViewManager(ViewManager viewManager) {
        if (viewManager == null) {
            throw new IllegalArgumentException("ViewManager darf nicht null sein");
        }
        this.viewManager = viewManager;
    }
    
    /**
     * Gibt den ViewManager zurück.
     * @return Die ViewManager-Instanz
     */
    protected ViewManager getViewManager() {
        if (viewManager == null) {
            throw new IllegalStateException("ViewManager wurde nicht initialisiert");
        }
        return viewManager;
    }
} 
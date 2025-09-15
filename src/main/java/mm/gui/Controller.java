package mm.gui;

/**
 * Basis-Controller-Klasse für alle GUI-Controller.
 */
public abstract class Controller {
    protected ViewManager viewManager;
    
    /**
     * Setzt den ViewManager für diesen Controller.
     * @param viewManager Der ViewManager
     */
    public void setViewManager(ViewManager viewManager) {
        this.viewManager = viewManager;
    }
    
    /**
     * Gibt den ViewManager zurück.
     * @return Der ViewManager
     */
    public ViewManager getViewManager() {
        return viewManager;
    }
} 
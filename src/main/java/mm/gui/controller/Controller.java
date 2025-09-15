package mm.gui.controller;

/**
 * Abstrakte Basisklasse für alle JavaFX-Controller im Projekt.
 * <p>
 * Stellt das Feld {@code viewManager} bereit, das von allen Controllern genutzt wird,
 * um zwischen Ansichten zu wechseln und globale Aktionen auszuführen.
 * </p>
 */
public abstract class Controller {
    /** Referenz auf den zentralen ViewManager für Szenenwechsel und Navigation */
    protected ViewManager viewManager;
    
    /**
     * Setzt den ViewManager für diesen Controller.
     * Wird beim Laden des FXML vom ViewManager aufgerufen.
     * @param viewManager Die zentrale ViewManager-Instanz
     */
    public void setViewManager(ViewManager viewManager) {
        this.viewManager = viewManager;
    }
    
    /**
     * @return Die aktuelle ViewManager-Instanz
     */
    public ViewManager getViewManager() {
        return viewManager;
    }
} 
package mm.editor.commands;

/**
 * Interface für alle Editor-Befehle, die rückgängig gemacht werden können.
 */
public interface EditorCommand {
    /**
     * Führt den Befehl aus.
     */
    void execute();
    
    /**
     * Macht den Befehl rückgängig.
     */
    void undo();
} 
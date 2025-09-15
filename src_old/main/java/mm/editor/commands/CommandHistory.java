package mm.editor.commands;

import java.util.Stack;

/**
 * Verwaltet die Historie der ausgeführten Befehle für Undo/Redo-Funktionalität.
 */
public class CommandHistory {
    private static final int MAX_HISTORY = 20;
    private final Stack<EditorCommand> undoStack = new Stack<>();
    private final Stack<EditorCommand> redoStack = new Stack<>();
    
    /**
     * Führt einen neuen Befehl aus und fügt ihn zur Historie hinzu.
     */
    public void executeCommand(EditorCommand command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear(); // Neue Aktion löscht Redo-Historie
        
        // Begrenze die Größe des Undo-Stacks
        if (undoStack.size() > MAX_HISTORY) {
            undoStack.remove(0);
        }
    }
    
    /**
     * Macht den letzten Befehl rückgängig.
     * @return true wenn ein Befehl rückgängig gemacht wurde, false wenn keine Befehle vorhanden
     */
    public boolean undo() {
        if (!undoStack.isEmpty()) {
            EditorCommand command = undoStack.pop();
            command.undo();
            redoStack.push(command);
            return true;
        }
        return false;
    }
    
    /**
     * Wiederholt den letzten rückgängig gemachten Befehl.
     * @return true wenn ein Befehl wiederholt wurde, false wenn keine Befehle vorhanden
     */
    public boolean redo() {
        if (!redoStack.isEmpty()) {
            EditorCommand command = redoStack.pop();
            command.execute();
            undoStack.push(command);
            return true;
        }
        return false;
    }
    
    /**
     * Löscht die gesamte Befehlshistorie.
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
    
    /**
     * Prüft, ob Undo verfügbar ist.
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }
    
    /**
     * Prüft, ob Redo verfügbar ist.
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
} 
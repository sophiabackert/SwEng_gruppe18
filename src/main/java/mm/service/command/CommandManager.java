package mm.service.command;

import javafx.scene.Node;
import javafx.scene.control.Button;
import java.util.Deque;
import java.util.ArrayDeque;

/**
 * Verwaltet Undo/Redo-Befehle für Aktionen im Editor (Command Pattern).
 * <p>
 * Unterstützt das Rückgängig-Machen und Wiederherstellen von Aktionen wie Hinzufügen, Löschen, Verschieben und Rotieren von Objekten.
 * Die Historie ist begrenzt, Buttons können optional angebunden werden.
 * </p>
 */
public class CommandManager {
    
    private final Deque<Action> undoStack = new ArrayDeque<>();
    private final Deque<Action> redoStack = new ArrayDeque<>();
    private static final int MAX_HISTORY = 20;
    
    private Button undoButton;
    private Button redoButton;
    
    /**
     * Erstellt einen CommandManager ohne Buttons.
     */
    public CommandManager() {}
    
    /**
     * Erstellt einen CommandManager mit Undo/Redo-Buttons.
     * @param undoButton Button für Undo
     * @param redoButton Button für Redo
     */
    public CommandManager(Button undoButton, Button redoButton) {
        this.undoButton = undoButton;
        this.redoButton = redoButton;
        updateButtonStates();
    }
    
    /**
     * Setzt die Buttons für Undo/Redo nachträglich.
     * @param undoButton Button für Undo
     * @param redoButton Button für Redo
     */
    public void setButtons(Button undoButton, Button redoButton) {
        this.undoButton = undoButton;
        this.redoButton = redoButton;
        updateButtonStates();
    }
    
    /**
     * Fügt eine Aktion zum Undo-Stack hinzu und leert den Redo-Stack.
     * @param action Die auszuführende Aktion
     */
    public void push(Action action) {
        undoStack.push(action);
        redoStack.clear();
        
        if (undoStack.size() > MAX_HISTORY) {
            undoStack.removeLast();
        }
        
        updateButtonStates();
    }
    
    /**
     * Macht die letzte Aktion rückgängig (Undo).
     */
    public void undo() {
        if (!undoStack.isEmpty()) {
            Action action = undoStack.pop();
            action.undo();
            redoStack.push(action);
            updateButtonStates();
        }
    }
    
    /**
     * Stellt die zuletzt rückgängig gemachte Aktion wieder her (Redo).
     */
    public void redo() {
        if (!redoStack.isEmpty()) {
            Action action = redoStack.pop();
            action.redo();
            undoStack.push(action);
            updateButtonStates();
        }
    }
    
    /**
     * Gibt zurück, ob ein Undo möglich ist.
     * @return true, wenn Undo möglich ist
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }
    
    /**
     * Gibt zurück, ob ein Redo möglich ist.
     * @return true, wenn Redo möglich ist
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
    
    /**
     * Leert die Undo- und Redo-Stacks.
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
        updateButtonStates();
    }
    
    /**
     * Aktualisiert die Aktivierung der Undo/Redo-Buttons.
     */
    private void updateButtonStates() {
        if (undoButton != null) {
            undoButton.setDisable(!canUndo());
        }
        if (redoButton != null) {
            redoButton.setDisable(!canRedo());
        }
    }
    
    /**
     * Interface für Aktionen, die rückgängig gemacht und wiederholt werden können.
     */
    public interface Action {
        void undo();
        void redo();
    }
    
    /**
     * Aktion zum Hinzufügen eines Objekts (Undo: entfernt, Redo: fügt hinzu).
     */
    public static class AddAction implements Action {
        private final Node node;
        private final Object object;
        private final java.util.List<Object> list;
        private final javafx.scene.Parent parent;
        
        /**
         * Erstellt eine AddAction.
         * @param object Das hinzuzufügende Objekt
         * @param node Die zugehörige Node
         * @param list Die Liste, in die eingefügt wird
         * @param parent Parent-Node (optional)
         */
        @SuppressWarnings("unchecked")
        public AddAction(Object object, Node node, java.util.List<?> list, javafx.scene.Parent parent) {
            this.object = object;
            this.node = node;
            this.list = (java.util.List<Object>) list;
            this.parent = parent;
        }
        
        @SuppressWarnings("unchecked")
        public AddAction(Object object, Node node, java.util.List<?> list, javafx.scene.layout.Pane parent) {
            this.object = object;
            this.node = node;
            this.list = (java.util.List<Object>) list;
            this.parent = parent;
        }
        
        @SuppressWarnings("unchecked")
        public AddAction(Object object, Node node, java.util.List<?> list) {
            this.object = object;
            this.node = node;
            this.list = (java.util.List<Object>) list;
            this.parent = null;
        }
        
        @Override
        public void undo() {
            list.remove(object);
            if (parent instanceof javafx.scene.layout.Pane) {
                ((javafx.scene.layout.Pane) parent).getChildren().remove(node);
            }
        }
        
        @Override
        public void redo() {
            list.add(object);
            if (parent instanceof javafx.scene.layout.Pane) {
                ((javafx.scene.layout.Pane) parent).getChildren().add(node);
            }
        }
    }
    
    /**
     * Aktion zum Löschen eines Objekts (Undo: fügt hinzu, Redo: entfernt).
     */
    public static class DeleteAction implements Action {
        private final Node node;
        private final Object object;
        private final java.util.List<Object> list;
        private final javafx.scene.Parent parent;
        
        @SuppressWarnings("unchecked")
        public DeleteAction(Object object, Node node, java.util.List<?> list, javafx.scene.Parent parent) {
            this.object = object;
            this.node = node;
            this.list = (java.util.List<Object>) list;
            this.parent = parent;
        }
        
        @SuppressWarnings("unchecked")
        public DeleteAction(Object object, Node node, java.util.List<?> list, javafx.scene.layout.Pane parent) {
            this.object = object;
            this.node = node;
            this.list = (java.util.List<Object>) list;
            this.parent = parent;
        }
        
        @SuppressWarnings("unchecked")
        public DeleteAction(Object object, Node node, java.util.List<?> list) {
            this.object = object;
            this.node = node;
            this.list = (java.util.List<Object>) list;
            this.parent = null;
        }
        
        @Override
        public void undo() {
            list.add(object);
            if (parent instanceof javafx.scene.layout.Pane) {
                ((javafx.scene.layout.Pane) parent).getChildren().add(node);
            }
        }
        
        @Override
        public void redo() {
            list.remove(object);
            if (parent instanceof javafx.scene.layout.Pane) {
                ((javafx.scene.layout.Pane) parent).getChildren().remove(node);
            }
        }
    }
    
    /**
     * Aktion zum Verschieben eines Objekts (Undo/Redo setzt die Position zurück).
     */
    public static class MoveAction implements Action {
        private final Node node;
        private final double oldX, oldY, newX, newY;
        
        /**
         * Erstellt eine MoveAction.
         * @param node Die zu bewegende Node
         * @param oldX Alte X-Position
         * @param oldY Alte Y-Position
         * @param newX Neue X-Position
         * @param newY Neue Y-Position
         */
        public MoveAction(Node node, double oldX, double oldY, double newX, double newY) {
            this.node = node;
            this.oldX = oldX;
            this.oldY = oldY;
            this.newX = newX;
            this.newY = newY;
        }
        
        @Override
        public void undo() {
            node.setLayoutX(oldX);
            node.setLayoutY(oldY);
        }
        
        @Override
        public void redo() {
            node.setLayoutX(newX);
            node.setLayoutY(newY);
        }
    }
    
    /**
     * Aktion zum Rotieren eines Objekts (Undo/Redo setzt die Rotation zurück).
     */
    public static class RotateAction implements Action {
        private final Node node;
        private final double oldRotation, newRotation;
        
        /**
         * Erstellt eine RotateAction.
         * @param node Die zu rotierende Node
         * @param oldRotation Alte Rotation
         * @param newRotation Neue Rotation
         */
        public RotateAction(Node node, double oldRotation, double newRotation) {
            this.node = node;
            this.oldRotation = oldRotation;
            this.newRotation = newRotation;
        }
        
        @Override
        public void undo() {
            node.setRotate(oldRotation);
        }
        
        @Override
        public void redo() {
            node.setRotate(newRotation);
        }
    }
} 
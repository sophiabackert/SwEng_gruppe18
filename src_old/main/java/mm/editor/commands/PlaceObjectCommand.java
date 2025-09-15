package mm.editor.commands;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import mm.objects.GameObject;

/**
 * Befehl zum Platzieren eines Spielobjekts auf der Zeichenfläche.
 */
public class PlaceObjectCommand implements EditorCommand {
    private final Pane editorCanvas;
    private final Node visualNode;
    private final GameObject gameObject;
    
    public PlaceObjectCommand(Pane editorCanvas, Node visualNode, GameObject gameObject) {
        this.editorCanvas = editorCanvas;
        this.visualNode = visualNode;
        this.gameObject = gameObject;
    }
    
    @Override
    public void execute() {
        if (!editorCanvas.getChildren().contains(visualNode)) {
            editorCanvas.getChildren().add(visualNode);
        }
    }
    
    @Override
    public void undo() {
        editorCanvas.getChildren().remove(visualNode);
    }
    
    /**
     * Gibt das platzierte GameObject zurück.
     */
    public GameObject getGameObject() {
        return gameObject;
    }
    
    /**
     * Gibt den visuellen Node zurück.
     */
    public Node getVisualNode() {
        return visualNode;
    }
} 
package mm.editor.commands;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import mm.objects.GameObject;

import java.util.Map;

/**
 * Befehl zum Löschen eines Spielobjekts von der Zeichenfläche.
 */
public class DeleteObjectCommand implements EditorCommand {
    private final Pane editorCanvas;
    private final Node visualNode;
    private final GameObject gameObject;
    private final Map<Node, GameObject> gameObjects;
    
    public DeleteObjectCommand(Pane editorCanvas, Node visualNode, 
                             GameObject gameObject, Map<Node, GameObject> gameObjects) {
        this.editorCanvas = editorCanvas;
        this.visualNode = visualNode;
        this.gameObject = gameObject;
        this.gameObjects = gameObjects;
    }
    
    @Override
    public void execute() {
        editorCanvas.getChildren().remove(visualNode);
        gameObjects.remove(visualNode);
    }
    
    @Override
    public void undo() {
        editorCanvas.getChildren().add(visualNode);
        gameObjects.put(visualNode, gameObject);
    }
} 
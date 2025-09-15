package mm.editor.commands;

import javafx.scene.Node;
import mm.objects.GameObject;

/**
 * Befehl zum Verschieben eines Spielobjekts.
 */
public class MoveObjectCommand implements EditorCommand {
    private final Node visualNode;
    private final GameObject gameObject;
    private final double oldX, oldY;
    private final double newX, newY;
    private static final double GAME_FIELD_WIDTH = 800;
    private static final double GAME_FIELD_HEIGHT = 600;
    private static final double BOUNDARY_PADDING = 10;
    
    public MoveObjectCommand(Node visualNode, GameObject gameObject, 
                           double oldX, double oldY, double newX, double newY) {
        this.visualNode = visualNode;
        this.gameObject = gameObject;
        this.oldX = oldX;
        this.oldY = oldY;
        
        // Stelle sicher, dass die neue Position innerhalb der Grenzen liegt
        this.newX = clampX(newX, visualNode.getBoundsInLocal().getWidth());
        this.newY = clampY(newY, visualNode.getBoundsInLocal().getHeight());
    }
    
    private double clampX(double x, double width) {
        return Math.min(Math.max(x, BOUNDARY_PADDING), 
                       GAME_FIELD_WIDTH - width - BOUNDARY_PADDING);
    }
    
    private double clampY(double y, double height) {
        return Math.min(Math.max(y, BOUNDARY_PADDING), 
                       GAME_FIELD_HEIGHT - height - BOUNDARY_PADDING);
    }
    
    @Override
    public void execute() {
        visualNode.setLayoutX(newX);
        visualNode.setLayoutY(newY);
        if (gameObject != null) {
            gameObject.setPosition((float)newX, (float)newY);
        }
    }
    
    @Override
    public void undo() {
        visualNode.setLayoutX(oldX);
        visualNode.setLayoutY(oldY);
        if (gameObject != null) {
            gameObject.setPosition((float)oldX, (float)oldY);
        }
    }
} 
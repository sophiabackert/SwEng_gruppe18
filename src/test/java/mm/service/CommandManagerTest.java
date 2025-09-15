package mm.service;

import javafx.scene.control.Button;
import mm.service.command.CommandManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CommandManagerTest {

    static class DummyAction implements CommandManager.Action {
        boolean undone = false;
        boolean redone = false;
        @Override public void undo() { undone = true; }
        @Override public void redo() { redone = true; }
    }

    @Test
    void testPushAndUndoRedo() {
        CommandManager cm = new CommandManager();
        DummyAction action = new DummyAction();
        cm.push(action);
        assertTrue(cm.canUndo());
        cm.undo();
        assertTrue(action.undone);
        assertTrue(cm.canRedo());
        cm.redo();
        assertTrue(action.redone);
    }

    @Test
    void testClearEmptiesStacks() {
        CommandManager cm = new CommandManager();
        cm.push(new DummyAction());
        cm.clear();
        assertFalse(cm.canUndo());
        assertFalse(cm.canRedo());
    }

    @Test
    void testUndoRedoWithButtons() {
        Button undoBtn = new Button();
        Button redoBtn = new Button();
        CommandManager cm = new CommandManager(undoBtn, redoBtn);
        DummyAction action = new DummyAction();
        cm.push(action);
        assertFalse(undoBtn.isDisabled());
        cm.undo();
        assertFalse(redoBtn.isDisabled());
        cm.clear();
        assertTrue(undoBtn.isDisabled());
        assertTrue(redoBtn.isDisabled());
    }

    @Test
    void testMaxHistoryLimit() {
        CommandManager cm = new CommandManager();
        for (int i = 0; i < 25; i++) {
            cm.push(new DummyAction());
        }
        // Es sollten maximal 20 im Stack sein
        int count = 0;
        while (cm.canUndo()) {
            cm.undo();
            count++;
        }
        assertEquals(20, count);
    }
} 
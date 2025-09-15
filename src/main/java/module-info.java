/**
 * The main module of the mm application.
 */
module mm {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires jbox2d.library;
    requires com.google.gson;

    opens mm.gui to javafx.fxml; // Important for Controller + FXML
    exports mm.gui;
    exports mm.gui.menu;
    opens mm.gui.menu to javafx.fxml;
    exports mm.game.level;
    opens mm.game.level to javafx.fxml;
}
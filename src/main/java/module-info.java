/**
 * The main module of the mm application.
 */
module mm {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires jbox2d.library;

    opens mm.gui to javafx.fxml; // Important for Controller + FXML
    exports mm.gui;
}
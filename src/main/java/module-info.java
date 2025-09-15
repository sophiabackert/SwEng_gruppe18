/**
 * Main module configuration for Mad Machines Prototype.
 */
module mm {
    // JavaFX Module-Abhängigkeiten
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires transitive javafx.graphics;
    requires jbox2d.library;
    requires com.google.gson;
    requires java.desktop;
    requires java.logging;
    requires java.prefs;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;

    // Exports für vorhandene Pakete
    exports mm;
    exports mm.gui;
    exports mm.core.config;
    exports mm.core.storage;
    exports mm.core.editor;

    // Opens für FXML und Serialisierung
    opens mm to javafx.graphics, javafx.fxml;
    opens mm.gui to javafx.fxml;
    opens mm.core.config to com.fasterxml.jackson.databind;
    opens mm.core.storage to com.fasterxml.jackson.databind;
}
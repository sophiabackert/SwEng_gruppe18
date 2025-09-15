/**
 * Main module configuration for Crazy Machines Prototype.
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

    // Exports für alle benötigten Pakete
    exports mm;
    exports mm.gui;
    exports mm.gui.components;
    exports mm.objects;
    exports mm.objects.misc;
    exports mm.objects.balls;
    exports mm.objects.components;
    exports mm.objects.containers;
    exports mm.objects.zones;
    exports mm.objects.pool;
    exports mm.engine;
    exports mm.editor.commands;
    exports mm.exceptions;
    exports mm.inventory;
    exports mm.model;
    exports mm.resources;
    exports mm.rules;
    exports mm.utils;
    exports mm.validation;
    exports mm.world;

    // Opens für FXML und Serialisierung
    opens mm to javafx.graphics, javafx.fxml;
    opens mm.gui to javafx.fxml;
    opens mm.gui.components to javafx.fxml;
    opens mm.objects to com.google.gson;
    opens mm.objects.misc to com.google.gson;
    opens mm.objects.balls to com.google.gson;
    opens mm.objects.components to com.google.gson;
    opens mm.objects.containers to com.google.gson;
    opens mm.objects.zones to com.google.gson;
    opens mm.model to com.google.gson;
    opens mm.utils to com.google.gson;
}
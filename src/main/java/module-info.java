

module mm {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive jbox2d.library;
    requires java.desktop;
    requires java.logging;
    requires java.prefs;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;

    exports mm.app;
    exports mm.domain.config;
    exports mm.domain.storage;
    exports mm.domain.editor;
    exports mm.domain.json;
    exports mm.service.command;
    exports mm.service.physics;
    exports mm.service.rendering;
    exports mm.service.object;
    exports mm.service.selection;
    exports mm.service.overlay;
    exports mm.service.collision;
    exports mm.gui.controller;
    exports mm.gui;

    opens mm.app to javafx.graphics, javafx.fxml;
    opens mm.gui.controller to javafx.fxml;
    opens mm.domain.config to com.fasterxml.jackson.databind;
    opens mm.domain.storage to com.fasterxml.jackson.databind;
}
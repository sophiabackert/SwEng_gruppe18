package mm.gui.components;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

/**
 * Factory-Klasse für einheitlich gestylte UI-Komponenten.
 */
public class UIComponents {
    
    /**
     * Erstellt einen Standard-Button mit einheitlichem Styling.
     * @param text Der Text des Buttons
     * @return Ein gestylter Button
     */
    public static Button createButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("menu-button");
        return button;
    }
    
    /**
     * Erstellt einen kleineren Button für Toolbars.
     * @param text Der Text des Buttons
     * @return Ein gestylter Toolbar-Button
     */
    public static Button createToolbarButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("editor-button");
        return button;
    }
    
    /**
     * Erstellt einen Slider mit Label und Wertebereich.
     * @param labelText Der Text des Labels
     * @param min Minimaler Wert
     * @param max Maximaler Wert
     * @param defaultValue Standardwert
     * @return Eine HBox mit Label und Slider
     */
    public static HBox createLabeledSlider(String labelText, double min, double max, double defaultValue) {
        Label label = new Label(labelText);
        label.getStyleClass().add("control-label");
        
        Slider slider = new Slider(min, max, defaultValue);
        slider.getStyleClass().add("custom-slider");
        
        Label valueLabel = new Label(String.format("%.1f", defaultValue));
        valueLabel.getStyleClass().add("value-label");
        slider.valueProperty().addListener((obs, oldVal, newVal) -> 
            valueLabel.setText(String.format("%.1f", newVal.doubleValue())));
        
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().addAll(label, slider, valueLabel);
        return container;
    }
    
    /**
     * Erstellt ein Textfeld mit Label.
     * @param labelText Der Text des Labels
     * @param defaultValue Standardwert
     * @return Eine HBox mit Label und Textfeld
     */
    public static HBox createLabeledTextField(String labelText, String defaultValue) {
        Label label = new Label(labelText);
        label.getStyleClass().add("control-label");
        
        TextField textField = new TextField(defaultValue);
        textField.getStyleClass().add("custom-textfield");
        
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().addAll(label, textField);
        return container;
    }
    
    /**
     * Erstellt eine Checkbox mit Label.
     * @param labelText Der Text des Labels
     * @param defaultValue Standardwert
     * @return Eine HBox mit Label und Checkbox
     */
    public static HBox createLabeledCheckbox(String labelText, boolean defaultValue) {
        Label label = new Label(labelText);
        label.getStyleClass().add("control-label");
        
        CheckBox checkbox = new CheckBox();
        checkbox.setSelected(defaultValue);
        checkbox.getStyleClass().add("custom-checkbox");
        
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().addAll(label, checkbox);
        return container;
    }
    
    /**
     * Erstellt eine ComboBox mit Label.
     * @param labelText Der Text des Labels
     * @param items Die Auswahlmöglichkeiten
     * @return Eine HBox mit Label und ComboBox
     */
    public static HBox createLabeledComboBox(String labelText, String... items) {
        Label label = new Label(labelText);
        label.getStyleClass().add("control-label");
        
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(items);
        comboBox.getStyleClass().add("custom-combo-box");
        if (items.length > 0) {
            comboBox.setValue(items[0]);
        }
        
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().addAll(label, comboBox);
        return container;
    }
    
    /**
     * Erstellt einen Container für Formularelemente.
     * @return Eine VBox mit Standardformatierung
     */
    public static VBox createFormContainer() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.TOP_LEFT);
        container.getStyleClass().add("form-container");
        return container;
    }
} 
package com.ramika.habit.gui;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Gui {

    /** Build and return the main scene */
    public Scene createMainScene() {
        Label label = new Label("Hello, JavaFX!");
        Button btn = new Button("Click me");
        btn.setOnAction(e -> label.setText("Clicked!"));

        VBox root = new VBox(12, label, btn);
        root.setStyle("-fx-padding: 24; -fx-alignment: center-left;");

        return new Scene(root, 480, 320);
    }

    /** Convenience: set up and show the primary stage */
    public void show(Stage stage) {
        stage.setTitle("Habit Tracker");
        stage.setScene(createMainScene());
        stage.show();
    }
}

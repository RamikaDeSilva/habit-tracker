package com.ramika.habit.gui;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AddHabitScene {
    public Scene create(Stage stage) {
        VBox layout = new VBox(10);
        TextField habitName = new TextField();
        habitName.setPromptText("Habit name");
        Button saveButton = new Button("Save");
        layout.getChildren().addAll(habitName, saveButton);
        return new Scene(layout, 400, 300);
    }
}

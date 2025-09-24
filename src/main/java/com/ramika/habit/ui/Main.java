package com.ramika.habit.ui;

import com.ramika.habit.gui.Gui;
import com.ramika.habit.service.Persistence;   // added
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Load from JSON before building UI
        Persistence.bootstrapLoad();

        new Gui().show(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

package com.ramika.habit.ui;

import com.ramika.habit.gui.Gui;
import com.ramika.habit.service.MidnightScheduler;   // <<< added
import com.ramika.habit.service.Persistence;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Load saved data before building the UI
        Persistence.bootstrapLoad();
        new Gui().show(primaryStage);
    }

    @Override
    public void stop() {
        // Tidy shutdown of the background scheduler
        MidnightScheduler.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

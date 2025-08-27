package com.ramika.habit.ui;

import com.ramika.habit.gui.Gui;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
//        Label label = new Label("Hello, JavaFX!");
//        Scene scene = new Scene(label, 400, 200);
//        primaryStage.setTitle("JavaFX Base App");
//        primaryStage.setScene(scene);
//        primaryStage.show();

        new Gui().show(primaryStage);   // delegate all UI to Gui
    }


    public static void main(String[] args) {
        launch(args);
        // new UserApp();
    }
}

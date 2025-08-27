package com.ramika.habit.gui;

import com.ramika.habit.model.Priority;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;

public class Gui {

    /** Build and return the main scene */
    public Scene createMainScene() {
        DashboardView dv = new DashboardView();
        Parent root = dv.create();

        ProgressCard prog = new ProgressCard();
        dv.contentBox().getChildren().add(prog);       // add above the cards
        prog.animateTo(0.34);

        // build your cards (sample)
        HabitCard c1 = new HabitCard("🏋️", "Morning Workout", "3 days/week");
        HabitCard c2 = new HabitCard("🧘", "Read 30 minutes", "5 days/week");

        // drop them into the dashboard content
        dv.contentBox().getChildren().addAll(c1, c2);

        CompletionSummaryCard summary = new CompletionSummaryCard();
        summary.animateToCounts(1, 3);  // example numbers for now

        // place it below the donut or wherever you like:
        dv.contentBox().getChildren().add(summary);

        return new Scene(root, 1000, 720);
    }

    /** Convenience: set up and show the primary stage */
    public void show(Stage stage) {
        stage.setTitle("Habit Tracker");
        stage.setScene(createMainScene());
        stage.show();
    }
}

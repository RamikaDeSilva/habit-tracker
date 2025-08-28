package com.ramika.habit.gui;

import com.ramika.habit.model.Priority;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;

public class Gui {

    /** Build and return the main scene */
    public Scene createMainScene() {



        DashboardView dv = new DashboardView();
        Parent root = dv.create();

        ScrollPane scroller = new ScrollPane(root);
        scroller.setFitToWidth(true);           // content stretches to window width
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroller.setPannable(true);
        scroller.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        ProgressCard prog = new ProgressCard();
        dv.contentBox().getChildren().add(prog);       // add above the cards
        prog.animateTo(0.34);

        // Weekly recap (TEMP demo data)
        WeeklyRecapCard recap = new WeeklyRecapCard();
        dv.contentBox().getChildren().add(recap);

        recap.setDayLabels(java.time.LocalDate.now());
// Demo percentages oldest→newest (7 numbers)
        recap.animateTo(new double[]{0.45, 0.62, 0.30, 0.80, 0.55, 0.41, 0.67});

        // build your cards (sample)
        HabitCard c1 = new HabitCard("🏋️", "Morning Workout", "3 days/week");
        HabitCard c2 = new HabitCard("🧘", "Read 30 minutes", "5 days/week");

        // drop them into the dashboard content
        dv.contentBox().getChildren().addAll(c1, c2);

        CompletionSummaryCard summary = new CompletionSummaryCard();
        summary.animateToCounts(1, 3);  // example numbers for now

        // place it below the donut or wherever you like:
        dv.contentBox().getChildren().add(summary);

        return new Scene(scroller, 1000, 720);
    }

    /** Convenience: set up and show the primary stage */
    public void show(Stage stage) {
        stage.setTitle("Habit Tracker");
        stage.setScene(createMainScene());
        stage.show();
    }
}

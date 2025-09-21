package com.ramika.habit.gui;

import com.ramika.habit.model.Habit;
import com.ramika.habit.service.HabitService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Gui {

    private ProgressCard prog;
    private WeeklyRecapCard recap;
    private CompletionSummaryCard summary;

    /** Build and return the main scene */
    public Scene createMainScene(Stage stage) {
        DashboardView dv = new DashboardView(stage);
        Parent root = dv.create();

        // Scroll container
        ScrollPane scroller = new ScrollPane(root);
        scroller.setFitToWidth(true);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroller.setPannable(true);
        scroller.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        // ── Top row: donut + weekly recap
        HBox progressBox = new HBox(20);
        progressBox.setPadding(new Insets(0, 0, 20, 0));
        progressBox.setAlignment(Pos.CENTER);

        prog  = new ProgressCard();
        recap = new WeeklyRecapCard();
        recap.setDayLabels(java.time.LocalDate.now());
        recap.animateTo(new double[]{0, 0, 0, 0, 0, 0, 0}); // placeholder until weekly data wired

        progressBox.getChildren().addAll(prog, recap);
        dv.contentBox().getChildren().add(progressBox);

        // Create summary (but add it later so it stays at the bottom)
        summary = new CompletionSummaryCard();
        summary.animateToCounts(0, 1); // initial state

        // ── Live listeners/bindings
        HabitService.percentDisplayedProperty().addListener((o, oldV, p) ->
                prog.animateTo(p == null ? 0.0 : p.doubleValue())
        );

        HabitService.completedDisplayedProperty().addListener((obs, oldV, newV) -> {
            if (summary != null) {
                summary.animateToCounts(newV.intValue(), HabitService.totalDisplayedProperty().get());
            }
        });

        HabitService.totalDisplayedProperty().addListener((obs, oldV, newV) -> {
            if (summary != null) {
                summary.animateToCounts(HabitService.completedDisplayedProperty().get(), newV.intValue());
            }
            // Rebuild cards when total changes (add/remove/deactivate)
            refreshHabitCards(dv);
        });

        // ── Build habit cards AFTER listeners are wired; summary will be appended last
        refreshHabitCards(dv);

        Scene scene = new Scene(scroller, 1200, 720);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        return scene;
    }

    /** Refresh all habit cards and keep summary at the bottom.
     *  Active-today habits appear first, inactive habits after. */
    private void refreshHabitCards(DashboardView dv) {
        // Remove existing HabitCard nodes and any existing summary instance
        dv.contentBox().getChildren().removeIf(node ->
                node instanceof HabitCard || node instanceof CompletionSummaryCard);

        // Partition habits
        List<Habit> activeToday = new ArrayList<>();
        List<Habit> inactive = new ArrayList<>();

        DayOfWeek today = LocalDate.now().getDayOfWeek();
        for (Habit h : HabitService.getAllHabits().values()) {
            if (h.getSchedule() != null && h.getSchedule().contains(today)) {
                activeToday.add(h);
            } else {
                inactive.add(h);
            }
        }

        // Sort alphabetically inside groups (optional)
        Comparator<Habit> byName = Comparator.comparing(h -> h.getName().toLowerCase());
        activeToday.sort(byName);
        inactive.sort(byName);

        // Add active cards first
        for (Habit h : activeToday) {
            HabitCard card = new HabitCard(pickIconFor(h), h.getName(), h.getSchedule().toString());
            card.bindToHabit(h);
            dv.contentBox().getChildren().add(card);
        }

        // Then add inactive
        for (Habit h : inactive) {
            HabitCard card = new HabitCard(pickIconFor(h), h.getName(), h.getSchedule().toString());
            card.bindToHabit(h);
            dv.contentBox().getChildren().add(card);
        }

        // Always append summary LAST
        VBox.setMargin(summary, new Insets(20, 0, 20, 0));
        dv.contentBox().getChildren().add(summary);

        // Initial sync after rebuild
        if (summary != null) {
            summaryUpdateSnapshot();
        }
    }

    /** Ensure summary + donut reflect current service state */
    private void summaryUpdateSnapshot() {
        int total = HabitService.totalDisplayedProperty().get();
        int done  = HabitService.completedDisplayedProperty().get();
        double p  = HabitService.percentDisplayedProperty().get();

        summary.animateToCounts(done, total);
        prog.animateTo(p);
    }

    //TODO - PICK ICON TO CATEGORY, NOT RANDOM NAME
    private String pickIconFor(Habit h) {
        String lower = h.getName() == null ? "" : h.getName().toLowerCase();
        if (lower.contains("workout") || lower.contains("run") || lower.contains("gym")) return "🏋️";
        if (lower.contains("read") || lower.contains("book")) return "📚";
        if (lower.contains("meditat") || lower.contains("breath")) return "🧘";
        return "✅";
    }

    /** Convenience: set up and show the primary stage */
    public void show(Stage stage) {
        stage.setTitle("Habit Tracker");
        stage.setScene(createMainScene(stage));
        stage.show();
    }
}

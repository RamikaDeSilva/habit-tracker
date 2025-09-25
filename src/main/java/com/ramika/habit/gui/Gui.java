package com.ramika.habit.gui;

import com.ramika.habit.model.Category;
import com.ramika.habit.model.Habit;
import com.ramika.habit.model.Priority;
import com.ramika.habit.service.HabitService;
import com.ramika.habit.service.MidnightScheduler;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
//import javafx.scene.layout.Priority as FxPriority;
import javafx.scene.layout.Region;
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

    // filter state (applies ONLY to active-today habits)
    private enum ActiveFilter { ALL, COMPLETED, REMAINING }
    private ActiveFilter activeFilter = ActiveFilter.ALL;

    // UI refs for counts on the pills
    private Label allCountLbl     = new Label("0");
    private Label doneCountLbl    = new Label("0");
    private Label remainCountLbl  = new Label("0");

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

        // Top row: donut + weekly recap
        HBox progressBox = new HBox(20);
        progressBox.setPadding(new Insets(0, 0, 20, 0));
        progressBox.setAlignment(Pos.CENTER);

        prog  = new ProgressCard();
        recap = new WeeklyRecapCard();
        recap.updateToday(new ArrayList<>(HabitService.getAllHabits().values()));

        progressBox.getChildren().addAll(prog, recap);
        dv.contentBox().getChildren().add(progressBox);

        // tiny filter bar (affects only active-today)
        HBox filterBar = buildFilterBar(dv);
        VBox.setMargin(filterBar, new Insets(0, 0, 12, 0));
        dv.contentBox().getChildren().add(filterBar);

        // completion summary (append later so it stays at bottom)
        summary = new CompletionSummaryCard();
        summary.animateToCounts(0, 1);

        // listeners → keep donut/summary/recap in sync
        HabitService.percentDisplayedProperty().addListener((o, oldV, p) ->
                prog.animateTo(p == null ? 0.0 : p.doubleValue())
        );

        HabitService.completedDisplayedProperty().addListener((obs, oldV, newV) -> {
            if (summary != null) {
                summary.animateToCounts(newV.intValue(), HabitService.totalDisplayedProperty().get());
            }
            updateFilterCounts();
            recap.updateToday(new ArrayList<>(HabitService.getAllHabits().values()));
        });

        HabitService.totalDisplayedProperty().addListener((obs, oldV, newV) -> {
            if (summary != null) {
                summary.animateToCounts(HabitService.completedDisplayedProperty().get(), newV.intValue());
            }
            refreshHabitCards(dv);
            updateFilterCounts();
            recap.updateToday(new ArrayList<>(HabitService.getAllHabits().values()));
        });

        // build habit cards
        refreshHabitCards(dv);
        updateFilterCounts();

        // refresh at local midnight
        MidnightScheduler.start(() -> Platform.runLater(() -> {
            HabitService.forceRecompute();
            recap.updateToday(new ArrayList<>(HabitService.getAllHabits().values()));
            refreshHabitCards(dv);
            updateFilterCounts();
            summaryUpdateSnapshot();
        }));

        Scene scene = new Scene(scroller, 1200, 720);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        return scene;
    }

    /** Build the pill filter bar (All / Completed / Remaining) */
    private HBox buildFilterBar(DashboardView dv) {
        Label prefix = new Label("Filter:");
        prefix.getStyleClass().add("filter-prefix");

        ToggleGroup tg = new ToggleGroup();

        ToggleButton allBtn = pill("📋", "All", allCountLbl);
        ToggleButton doneBtn = pill("✅", "Completed", doneCountLbl);
        ToggleButton remainBtn = pill("⏳", "Remaining", remainCountLbl);

        allBtn.setToggleGroup(tg);
        doneBtn.setToggleGroup(tg);
        remainBtn.setToggleGroup(tg);

        allBtn.setSelected(true);

        allBtn.setOnAction(e -> { activeFilter = ActiveFilter.ALL;       refreshHabitCards(dv); });
        doneBtn.setOnAction(e -> { activeFilter = ActiveFilter.COMPLETED; refreshHabitCards(dv); });
        remainBtn.setOnAction(e -> { activeFilter = ActiveFilter.REMAINING; refreshHabitCards(dv); });

        HBox box = new HBox(12, prefix, allBtn, doneBtn, remainBtn);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    /** Small helper to create a pill with icon, label, and count bubble */
    private ToggleButton pill(String icon, String label, Label countLbl) {
        Label text = new Label(label);
        Label emoji = new Label(icon);
        emoji.getStyleClass().add("pill-emoji");

        countLbl.getStyleClass().add("pill-count");

        HBox inner = new HBox(8, emoji, text, spacer(6), countLbl);
        inner.setAlignment(Pos.CENTER_LEFT);

        ToggleButton tb = new ToggleButton();
        tb.setGraphic(inner);
        tb.getStyleClass().add("pill");
        tb.setFocusTraversable(false);
        return tb;
    }

    private Region spacer(double w) {
        Region r = new Region();
        r.setMinWidth(w);
        r.setPrefWidth(w);
        r.setMaxWidth(w);
        return r;
    }

    /**
     * Refresh all habit cards and keep summary at the bottom.
     * Active-today habits appear first, inactive habits after.
     * Inside each group, order by Priority: HIGH → MEDIUM → LOW, then by name.
     * The filter applies ONLY to the active-today list.
     */
    private void refreshHabitCards(DashboardView dv) {
        dv.contentBox().getChildren().removeIf(node ->
                node instanceof HabitCard || node instanceof CompletionSummaryCard);

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

        // apply filter to activeToday only
        List<Habit> filteredActive = new ArrayList<>();
        for (Habit h : activeToday) {
            boolean done = isCompletedToday(h);
            switch (activeFilter) {
                case ALL       -> filteredActive.add(h);
                case COMPLETED -> { if (done) filteredActive.add(h); }
                case REMAINING -> { if (!done) filteredActive.add(h); }
            }
        }

        // Comparator: by priority (high→low), then by name
        Comparator<Habit> byPriorityThenName = Comparator
                .comparingInt((Habit h) -> priorityRank(h.getPriority()))
                .thenComparing(h -> safeLower(h.getName()));

        filteredActive.sort(byPriorityThenName);
        inactive.sort(byPriorityThenName);

        // render active first
        for (Habit h : filteredActive) {
            HabitCard card = new HabitCard(pickIconFor(h), h.getName(), h.getSchedule().toString());
            card.bindToHabit(h);
            dv.contentBox().getChildren().add(card);
        }

        // render inactive after (only when "All" filter)
        if (activeFilter == ActiveFilter.ALL) {
            for (Habit h : inactive) {
                HabitCard card = new HabitCard(pickIconFor(h), h.getName(), h.getSchedule().toString());
                card.bindToHabit(h);
                dv.contentBox().getChildren().add(card);
            }
        }

        VBox.setMargin(summary, new Insets(20, 0, 20, 0));
        dv.contentBox().getChildren().add(summary);

        if (summary != null) {
            summaryUpdateSnapshot();
        }

        updateFilterCounts();
    }

    /** Ensure summary + donut reflect current service state */
    private void summaryUpdateSnapshot() {
        int total = HabitService.totalDisplayedProperty().get();
        int done  = HabitService.completedDisplayedProperty().get();
        double p  = HabitService.percentDisplayedProperty().get();

        summary.animateToCounts(done, total);
        prog.animateTo(p);
    }

    // compute counts for the pills
    private void updateFilterCounts() {
        DayOfWeek today = LocalDate.now().getDayOfWeek();

        int totalAll = HabitService.getAllHabits().size(); // active + inactive
        int activeCompleted = 0;
        int activeRemaining = 0;

        for (Habit h : HabitService.getAllHabits().values()) {
            boolean isActiveToday = h.getSchedule() != null && h.getSchedule().contains(today);
            if (!isActiveToday) continue;

            if (isCompletedToday(h)) activeCompleted++;
            else activeRemaining++;
        }

        allCountLbl.setText(String.valueOf(totalAll));
        doneCountLbl.setText(String.valueOf(activeCompleted));
        remainCountLbl.setText(String.valueOf(activeRemaining));
    }

    /** Pick icon by Category or name */
    private String pickIconFor(Habit h) {
        try {
            Category cat = h.getCategory();
            if (cat != null) {
                return switch (cat) {
                    case FITNESS       -> "\uD83C\uDFCB\uFE0F";
                    case FINANCIAL     -> "\uD83D\uDCB0";
                    case MENTALHEALTH  -> "\uD83E\uDDE0";
                    case OTHER         -> "⭐";
                };
            }
        } catch (Throwable ignored) {}

        String lower = h.getName() == null ? "" : h.getName().toLowerCase();
        if (lower.contains("workout") || lower.contains("run") || lower.contains("gym")) return "🏋️";
        if (lower.contains("read") || lower.contains("book")) return "📚";
        if (lower.contains("meditat") || lower.contains("breath")) return "🧘";
        return "✅";
    }

    // centralized way to ask if a habit is completed today
    private boolean isCompletedToday(Habit h) {
        try {
            return h.isCompletedToday();
        } catch (Throwable ignored) {
            return false;
        }
    }

    // priority sorting helper (HIGH -> 0, MEDIUM -> 1, LOW -> 2)
    private int priorityRank(Priority p) {
        if (p == null) return 2;
        return switch (p) {
            case HIGH -> 0;
            case MEDIUM -> 1;
            case LOW -> 2;
        };
    }

    private String safeLower(String s) { return s == null ? "" : s.toLowerCase(); }

    /** Show the primary stage */
    public void show(Stage stage) {
        stage.setTitle("Habit Hero - Track Your Habits, Achieve Your Goals");
        stage.setScene(createMainScene(stage));
        //stage.setResizable(false);
        stage.show();
    }
}

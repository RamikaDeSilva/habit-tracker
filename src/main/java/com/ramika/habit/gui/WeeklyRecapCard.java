package com.ramika.habit.gui;

import com.ramika.habit.model.Habit;
import com.ramika.habit.model.Status;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WeeklyRecapCard extends VBox {
    private static final int DAYS = 7;
    private static final double MAX_BAR_H = 110; // bar height in px
    private static final double BAR_W = 56;      // bar width in px
    private static final double BAR_GUTTER = 18; // spacing between columns

    private final List<Rectangle> fills = new ArrayList<>();
    private final List<Label> dayLabels = new ArrayList<>();

    public WeeklyRecapCard() {
        setSpacing(16);
        setPadding(new Insets(22));
        setAlignment(Pos.TOP_LEFT);
        setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(20), Insets.EMPTY)));
        setEffect(new DropShadow(20, Color.rgb(15, 23, 42, 0.10)));
        setMaxWidth(760);
        setPrefWidth(500);

        // Title
        Label title = new Label("Weekly Recap");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #0f172a;");
        getChildren().add(title);

        // Bars row
        GridPane grid = new GridPane();
        grid.setHgap(BAR_GUTTER);
        grid.setAlignment(Pos.CENTER_LEFT);

        for (int i = 0; i < DAYS; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / DAYS);
            cc.setHalignment(javafx.geometry.HPos.CENTER);
            grid.getColumnConstraints().add(cc);
        }

        for (int i = 0; i < DAYS; i++) {
            VBox one = new VBox(8);
            one.setAlignment(Pos.BOTTOM_CENTER);

            StackPane slot = new StackPane();
            slot.setPrefHeight(MAX_BAR_H);
            slot.setMinHeight(MAX_BAR_H);
            slot.setMaxHeight(MAX_BAR_H);

            Rectangle track = new Rectangle(BAR_W, MAX_BAR_H, Color.web("#e5e7eb"));
            track.setArcWidth(12); track.setArcHeight(12);

            Rectangle fill = new Rectangle(BAR_W, 0, Color.web("#34d399"));
            fill.setArcWidth(12); fill.setArcHeight(12);
            StackPane.setAlignment(fill, Pos.BOTTOM_CENTER);

            slot.getChildren().addAll(track, fill);
            fills.add(fill);

            Label day = new Label("—");
            day.setStyle("-fx-text-fill:#475569; -fx-font-size:13px;");
            dayLabels.add(day);

            one.getChildren().addAll(slot, day);
            grid.add(one, i, 0);
        }

        getChildren().add(grid);

        // Footer
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setSpacing(12);
        Label left = new Label("Completion rate (%)");
        left.setStyle("-fx-text-fill:#64748b; -fx-font-size: 13px;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Label right = new Label("Last 7 days");
        right.setStyle("-fx-text-fill:#64748b; -fx-font-size: 13px;");
        footer.getChildren().addAll(left, spacer, right);

        getChildren().add(footer);
    }

    /** Convenience: compute & animate for the last 7 days ending today. */
    public void updateToday(List<Habit> habits) {
        updateFromHabits(habits, LocalDate.now());
    }

    /** Compute & animate for the last 7 days ending at endDate. */
    public void updateFromHabits(List<Habit> habits, LocalDate endDate) {
        setDayLabels(endDate);                       // Sun..Sat labels
        double[] pcts = computePercentages(habits, endDate); // 0..1 per day
        animateTo(pcts);
    }

    /** Show Sun..Sat for the last 7 days ending at endDate (Sunday-first). */
    public void setDayLabels(LocalDate endDate) {
        List<LocalDate> days = last7DaysSundayFirst(endDate);
        for (int i = 0; i < DAYS; i++) {
            DayOfWeek dow = days.get(i).getDayOfWeek();
            String shortName = dow.getDisplayName(TextStyle.SHORT, Locale.getDefault());
            dayLabels.get(i).setText(shortName);
        }
    }

    /** Daily completion rates (0..1) for last 7 days ending at endDate, Sunday-first. */
    private double[] computePercentages(List<Habit> habits, LocalDate endDate) {
        double[] out = new double[DAYS];
        List<LocalDate> days = last7DaysSundayFirst(endDate); // Sun..Sat

        for (int i = 0; i < DAYS; i++) {
            LocalDate date = days.get(i);
            DayOfWeek dow = date.getDayOfWeek();

            int scheduled = 0;
            int completed = 0;

            if (habits != null) {
                for (Habit h : habits) {
                    if (h == null) continue;
                    if (h.getActiveStatus() == Status.ACTIVE
                            && h.getSchedule() != null
                            && h.getSchedule().contains(dow)) {
                        scheduled++;
                        if (h.isCompletedOn(date)) {
                            completed++;
                        }
                    }
                }
            }

            out[i] = (scheduled == 0) ? 0.0 : ((double) completed) / scheduled;
        }
        return out;
    }

    /** Animate the bars to the given percentages (0..1), Sunday-first. */
    public void animateTo(double[] percentages) {
        for (int i = 0; i < Math.min(DAYS, percentages.length); i++) {
            double pct = clamp(percentages[i], 0, 1);
            Rectangle fill = fills.get(i);

            double fromH = fill.getHeight();
            double toH   = pct * MAX_BAR_H;

            Timeline tl = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(fill.heightProperty(), fromH)),
                    new KeyFrame(Duration.millis(600),
                            new KeyValue(fill.heightProperty(), toH, Interpolator.EASE_BOTH))
            );
            tl.play();
        }
    }

    /** Build the last 7 dates ending at endDate, rotated so Sunday is index 0 (Sun..Sat). */
    private List<LocalDate> last7DaysSundayFirst(LocalDate endDate) {
        List<LocalDate> window = new ArrayList<>(DAYS);
        LocalDate start = endDate.minusDays(DAYS - 1); // oldest..newest
        for (int i = 0; i < DAYS; i++) window.add(start.plusDays(i));

        int sundayIdx = -1;
        for (int i = 0; i < DAYS; i++) {
            if (window.get(i).getDayOfWeek().getValue() == 7) { // 7 == Sunday
                sundayIdx = i; break;
            }
        }
        if (sundayIdx == -1) return window; // fallback

        List<LocalDate> rotated = new ArrayList<>(DAYS);
        for (int i = 0; i < DAYS; i++) {
            rotated.add(window.get((sundayIdx + i) % DAYS));
        }
        return rotated;
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}

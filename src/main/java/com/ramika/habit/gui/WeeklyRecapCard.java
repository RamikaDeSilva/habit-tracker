package com.ramika.habit.gui;

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
    private static final double BAR_W = 56;

    private final List<Rectangle> fills = new ArrayList<>();
    private final List<Label> dayLabels = new ArrayList<>();

    public WeeklyRecapCard() {
        setSpacing(16);
        setPadding(new Insets(22));
        setAlignment(Pos.TOP_LEFT);
        setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(20), Insets.EMPTY)));
        setEffect(new DropShadow(20, Color.rgb(15, 23, 42, 0.10)));
        setMaxWidth(760);

        // Title
        Label title = new Label("Weekly Recap");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #0f172a;");
        getChildren().add(title);

        // Bars row: 7 equal columns
        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setAlignment(Pos.CENTER_LEFT);

// 7 columns, each 1/7 of the width
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

            // keep bars a nice fixed width (looks like your Figma ~48–56px)
            double barW = 56;

            Rectangle track = new Rectangle(barW, MAX_BAR_H, Color.web("#e5e7eb"));
            track.setArcWidth(12); track.setArcHeight(12);

            Rectangle fill = new Rectangle(barW, 0, Color.web("#34d399"));
            fill.setArcWidth(12); fill.setArcHeight(12);
            StackPane.setAlignment(fill, Pos.BOTTOM_CENTER);

            slot.getChildren().addAll(track, fill);
            fills.add(fill);

            Label day = new Label("—");
            day.setStyle("-fx-text-fill:#475569; -fx-font-size:13px;");
            dayLabels.add(day);

            one.getChildren().addAll(slot, day);

            // put this day in column i, row 0
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

    /** Update labels to show the last 7 days ending at endDate (rightmost bar). */
    public void setDayLabels(LocalDate endDate) {
        // order: oldest -> newest (left to right)
        LocalDate start = endDate.minusDays(DAYS - 1);
        for (int i = 0; i < DAYS; i++) {
            LocalDate d = start.plusDays(i);
            DayOfWeek dow = d.getDayOfWeek();
            String shortName = dow.getDisplayName(TextStyle.SHORT, Locale.getDefault()); // Mon, Tue...
            dayLabels.get(i).setText(shortName);
        }
    }

    /** Animate the bars to the given percentages (0..1), oldest->newest. */
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

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}


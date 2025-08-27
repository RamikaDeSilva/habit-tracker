package com.ramika.habit.gui;

import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class LinearProgressBar extends StackPane {
    private final DoubleProperty progress = new SimpleDoubleProperty(0); // 0..1

    private final Rectangle track = new Rectangle();
    private final Rectangle fill  = new Rectangle();

    public LinearProgressBar(double width, double height) {
        setPrefSize(width, height);
        setMinSize(width, height);
        setMaxWidth(width);
        setAlignment(Pos.CENTER_LEFT);

        // pill corners
        double radius = height;
        track.setArcWidth(radius);
        track.setArcHeight(radius);
        fill.setArcWidth(radius);
        fill.setArcHeight(radius);

        // colors
        track.setFill(Color.web("#e5e7eb")); // light gray
        fill.setFill(new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0.00, Color.web("#f59e0b")),   // amber
                new Stop(0.55, Color.web("#a3c2d1")),   // soft mid
                new Stop(0.95, Color.web("#60a5fa"))    // blue
        ));

        // size bindings
        track.widthProperty().bind(widthProperty());
        track.heightProperty().bind(heightProperty());

        fill.heightProperty().bind(heightProperty());
        // width follows progress (0..1) of track width
        fill.widthProperty().bind(track.widthProperty().multiply(progress));

        getChildren().addAll(track, fill);
    }

    public void setProgress(double p) {
        progress.set(Math.max(0, Math.min(1, p)));
    }

    public void animateTo(double target) {
        target = Math.max(0, Math.min(1, target));
        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progress, progress.get())),
                new KeyFrame(Duration.millis(900),
                        new KeyValue(progress, target, Interpolator.EASE_BOTH))
        );
        tl.play();
    }
}

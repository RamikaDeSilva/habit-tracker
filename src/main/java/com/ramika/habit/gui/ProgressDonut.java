package com.ramika.habit.gui;

import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;

public class ProgressDonut extends StackPane {
    private final DoubleProperty progress = new SimpleDoubleProperty(0); // 0..1
    private final Arc arc = new Arc();
    private final Label pct = new Label("0%");
    private final Label sub = new Label("complete");

    public ProgressDonut(double size) {
        setPrefSize(size, size);
        setMinSize(size, size);
        setMaxSize(size, size);
        setAlignment(Pos.CENTER);

        // ring drawing area
        Pane ring = new Pane();
        ring.setPrefSize(size, size);

        // track (light gray circle)
        Circle track = new Circle();
        track.setFill(Color.TRANSPARENT);
        track.setStroke(Color.web("#e5e7eb")); // neutral-200
        track.setStrokeWidth(16);
        track.setStrokeLineCap(StrokeLineCap.ROUND);

        // progress arc (orange)
        arc.setType(ArcType.OPEN);
        arc.setFill(Color.TRANSPARENT);
        arc.setStroke(Color.web("#f59e0b"));   // amber-500
        arc.setStrokeWidth(16);
        arc.setStrokeLineCap(StrokeLineCap.ROUND);
        arc.setStartAngle(90);                 // start at top (12 o’clock)
        arc.setLength(0);                      // animated later

        // center + radius bindings so it stays centered
        var rBinding = Bindings.min(ring.widthProperty(), ring.heightProperty())
                .divide(2).subtract(12); // 12px inner padding
        track.centerXProperty().bind(ring.widthProperty().divide(2));
        track.centerYProperty().bind(ring.heightProperty().divide(2));
        track.radiusProperty().bind(rBinding);

        arc.centerXProperty().bind(ring.widthProperty().divide(2));
        arc.centerYProperty().bind(ring.heightProperty().divide(2));
        arc.radiusXProperty().bind(rBinding);
        arc.radiusYProperty().bind(rBinding);

        ring.getChildren().addAll(track, arc);

        // center labels
        pct.setStyle("-fx-font-size: 32px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");
        sub.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        var textBox = new javafx.scene.layout.VBox(2, pct, sub);
        textBox.setAlignment(Pos.CENTER);

        getChildren().addAll(ring, textBox);

        // update arc/label when progress changes
        progress.addListener((obs, oldV, v) -> {
            double p = Math.max(0, Math.min(1, v.doubleValue()));
            arc.setLength(-360 * p);                    // negative = clockwise
            pct.setText(Math.round(p * 100) + "%");
        });
    }

    /** instantly set progress 0..1 */
    public void setProgress(double p) { progress.set(p); }

    /** animate from current to target 0..1 */
    public void animateTo(double target) {
        target = Math.max(0, Math.min(1, target));
        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO,      new KeyValue(progress, progress.get())),
                new KeyFrame(Duration.millis(900), new KeyValue(progress, target, Interpolator.EASE_BOTH))
        );
        tl.play();
    }
}


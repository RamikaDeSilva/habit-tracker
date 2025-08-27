package com.ramika.habit.gui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


public class HabitCard extends StackPane {
    private final BooleanProperty completed = new SimpleBooleanProperty(false);

    private final StackPane checkbox;   // left box we click
    private final Rectangle box;        // the square outline/background
    private final Label check;          // the ✓ label


    public HabitCard(String emoji, String title, String frequencyText) {
        // card surface
        setPadding(new Insets(18));
        setBackground(new Background(new BackgroundFill(Color.web("#FCFCFF"), new CornerRadii(20), Insets.EMPTY)));
        setEffect(new DropShadow(12, Color.rgb(0, 0, 0, 0.12)));

        // left checkbox
        box = new Rectangle(28, 28);
        box.setArcWidth(10);
        box.setArcHeight(10);
        box.setFill(Color.WHITE);
        box.setStroke(Color.web("#D1D5DB"));
        box.setStrokeWidth(1.5);

        check = new Label("✓");
        check.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        check.setVisible(false);

        StackPane greenFill = new StackPane(check);
        greenFill.setBackground(new Background(new BackgroundFill(Color.web("#16a34a"), new CornerRadii(8), Insets.EMPTY)));
        greenFill.setMaxSize(24, 24);
        greenFill.setVisible(false);

        checkbox = new StackPane(box, greenFill);
        checkbox.setMinSize(32, 32);
        checkbox.setAlignment(Pos.CENTER_LEFT);
        checkbox.setCursor(Cursor.HAND);

        // title + frequency
        Label titleLbl = new Label(emoji + "  " + title);
        titleLbl.setStyle("-fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: #111827;");

        Label freqLbl = new Label("📅  " + frequencyText);
        freqLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280; -fx-font-weight: 600;");

        VBox textCol = new VBox(6, titleLbl, freqLbl);
        textCol.setAlignment(Pos.CENTER_LEFT);

        HBox row = new HBox(12, checkbox, textCol);
        row.setAlignment(Pos.CENTER_LEFT);

        getChildren().add(row);

        // behavior: toggle completed on click
        checkbox.setOnMouseClicked(e -> completed.set(!completed.get()));
        completed.addListener((obs, was, is) -> {
            // update visuals for now (animation comes in Step 2)
            greenFill.setVisible(is);
            check.setVisible(is);
            box.setStroke(is ? Color.web("#16a34a") : Color.web("#D1D5DB"));
        });
    }

    // expose a property so other classes can bind if needed
    public BooleanProperty completedProperty() { return completed; }
    public boolean isCompleted() { return completed.get(); }
    public void setCompleted(boolean value) { completed.set(value); }

}

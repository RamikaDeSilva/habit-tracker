package com.ramika.habit.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class ProgressCard extends VBox {
    private final ProgressDonut donut;

    public ProgressCard() {
        setSpacing(16);
        setPadding(new Insets(28));
        setAlignment(Pos.TOP_CENTER);

        setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(20), Insets.EMPTY)));
        setEffect(new DropShadow(20, Color.rgb(15, 23, 42, 0.10))); // soft card shadow
        //setMaxWidth(400);  // keep width Figma
        setPrefWidth(500);

        Label title = new Label("Today's Progress");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: #0f172a;");

        donut = new ProgressDonut(220);

        getChildren().addAll(title, donut);

        //TODO
        // Make the card wider
    }

    public void animateTo(double p) { donut.animateTo(p); }
    public void setProgress(double p) { donut.setProgress(p); }
}

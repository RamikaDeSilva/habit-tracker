package com.ramika.habit.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class CompletionSummaryCard extends VBox {
    private final Text completedTxt = new Text("0");
    private final Text totalTxt     = new Text("0");
    private final LinearProgressBar bar;

    private int completed = 0;
    private int total     = 1;

    public CompletionSummaryCard() {
        setSpacing(18);
        setPadding(new Insets(28, 28, 28, 28));
        setAlignment(Pos.TOP_CENTER);
        setMaxWidth(980);

        // soft blue → lavender background like Figma
        setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 1, 1, true, null,
                        new Stop(0.0, Color.web("#dbeafe")),
                        new Stop(1.0, Color.web("#f5e1ff"))
                ),
                new CornerRadii(26), Insets.EMPTY
        )));
        setEffect(new DropShadow(24, Color.rgb(15, 23, 42, 0.12)));

        // header row (emoji centered)
        Text emoji = new Text("💪");
        emoji.setFont(Font.font("System", FontWeight.BOLD, 28));

        // "X of Y habits completed" (X green)
        Text pre  = new Text("  ");
        Text of   = new Text(" of ");
        Text mid  = new Text(" habits completed");

        pre.getStyleClass().add("completion-text");
        of.getStyleClass().add("completion-text");
        mid.getStyleClass().add("completion-text");

        completedTxt.getStyleClass().add("completion-text");
        completedTxt.setId("completed-text");
        totalTxt.setId("total-text");
        totalTxt.getStyleClass().add("completion-text");

        TextFlow line = new TextFlow(emoji, pre, completedTxt, of, totalTxt, mid);
        line.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        line.setLineSpacing(4);

        // bar
        bar = new LinearProgressBar(900, 14);

        getChildren().addAll(line, bar);
    }

    private double ratio() { return total <= 0 ? 0 : (double) completed / (double) total; }

    public void setCounts(int completed, int total) {
        this.completed = Math.max(0, completed);
        this.total = Math.max(1, total);
        completedTxt.setText(String.valueOf(this.completed));
        totalTxt.setText(String.valueOf(this.total));
        bar.setProgress(ratio());
    }

    public void animateToCounts(int completed, int total) {
        this.completed = Math.max(0, completed);
        this.total = Math.max(1, total);
        completedTxt.setText(String.valueOf(this.completed));
        totalTxt.setText(String.valueOf(this.total));
        bar.animateTo(ratio());
    }
}


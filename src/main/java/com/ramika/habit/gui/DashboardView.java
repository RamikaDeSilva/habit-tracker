package com.ramika.habit.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DashboardView {

    private VBox content;  // <- where the rest of the UI goes

    public Parent create() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(24));
        root.getChildren().addAll(buildTopNav(), buildHeader());

        content = new VBox(16);                 // your cards live here
        content.setAlignment(Pos.TOP_LEFT);
        root.getChildren().add(content);

        return root;
    }

    public VBox contentBox() {                 // expose it to Gui
        return content;
    }


    private Node buildTopNav() {
        HBox nav = new HBox(12);
        nav.setAlignment(Pos.CENTER_LEFT);

        Button dashboard = new Button("Dashboard");
        dashboard.setStyle(
                "-fx-background-color: #0f172a; -fx-text-fill: white; -fx-font-weight: 700; " +
                        "-fx-padding: 10 16; -fx-background-radius: 20;"
        );

        Button addHabit = new Button("+  Add Habit");
        addHabit.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #2563eb; -fx-font-weight: 700; " +
                        "-fx-padding: 10 12;"
        );

        nav.getChildren().addAll(dashboard, addHabit);
        return nav;
    }

    // --- header: date line + big title + supportive line
    private Node buildHeader() {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER_LEFT);

        String dateText = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d"));
        Label date = new Label("📅  " + dateText);
        date.setStyle("-fx-text-fill: #475569; -fx-font-size: 16px;");

        Label title = new Label("Your Habits Today");
        title.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 40));
        title.setTextFill(Color.web("#0f172a"));

        Label sub = new Label("💪 Great progress! Keep it up!");
        sub.setStyle("-fx-text-fill: #64748b; -fx-font-size: 18px;");

        box.getChildren().addAll(date, title, sub);
        return box;
    }
}


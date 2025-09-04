// DashboardView.java
package com.ramika.habit.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DashboardView {
    private VBox content;
    private Stage stage;

    public DashboardView(Stage stage) {
        this.stage = stage;
        content = new VBox();
    }


    public Parent create() {
        BorderPane shell = new BorderPane();
        shell.setPadding(new Insets(24));

        // TOP: nav stays left
        HBox nav = buildTopNav();
        nav.setAlignment(Pos.CENTER_LEFT);
        shell.setTop(nav);

        // CENTER: a centered column that holds the header + content
        VBox centerCol = new VBox(18);
        centerCol.setAlignment(Pos.TOP_CENTER);   // <- center horizontally
        centerCol.getChildren().addAll(buildHeader(), createContent());
        shell.setCenter(centerCol);

        return shell;
    }

    public VBox contentBox() { return content; }

    private VBox createContent() {
        content = new VBox(16);
        content.setAlignment(Pos.TOP_CENTER);     // cards/progress will center too
        return content;
    }

    private HBox buildTopNav() {
        HBox nav = new HBox(12);
        Button dashboard = new Button("Dashboard");
        dashboard.setStyle("-fx-background-color:#0f172a;-fx-text-fill:white;-fx-font-weight:700;"
                + "-fx-padding:10 16;-fx-background-radius:20;");
        Button addHabit = new Button("+  Add Habit");
        addHabit.setStyle("-fx-background-color:transparent;-fx-text-fill:#2563eb;-fx-font-weight:700;"
                + "-fx-padding:10 12;");

        addHabit.setOnAction(e -> {
            AddHabitScene addHabitScene = new AddHabitScene();
            Scene scene = addHabitScene.create(stage);
            stage.setScene(scene);
        });

        nav.getChildren().addAll(dashboard, addHabit);
        return nav;
    }

    private Node buildHeader() {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);             // <- center the three lines

        String dateText = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d"));
        Label date = new Label("📅  " + dateText);
        date.setStyle("-fx-text-fill:#475569;-fx-font-size:16px;");
        date.setTextAlignment(TextAlignment.CENTER);

        Label title = new Label("Your Habits Today");
        title.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 40));
        title.setTextFill(Color.web("#0f172a"));
        title.setTextAlignment(TextAlignment.CENTER);

        Label sub = new Label("💪 Great progress! Keep it up!");
        sub.setStyle("-fx-text-fill:#64748b;-fx-font-size:18px;");
        sub.setTextAlignment(TextAlignment.CENTER);

        box.getChildren().addAll(date, title, sub);
        return box;
    }
}


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

    // Header elements
    private VBox hero;
    private Label dateText;
    private Label subIcon;
    private Label subText;

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
            AddHabitScene addHabitScene = new AddHabitScene(stage);
            //Scene scene = addHabitScene.create(stage);
            Scene scene = addHabitScene.getScene();
            stage.setScene(scene);
        });

        nav.getChildren().addAll(dashboard, addHabit);
        return nav;
    }

    private Node buildHeader() {

        hero = new VBox();
        hero.getStyleClass().addAll("hero");        // base
        // If ALL habits complete, also add "completed"
        // hero.getStyleClass().add("completed");

        HBox dateRow = new HBox();
        dateRow.getStyleClass().addAll("hero-row");
        Label dateIcon = new Label("\uD83D\uDCC5");   // 📅
        dateIcon.getStyleClass().add("hero-icon");

        dateText = new Label(formattedDate());  // e.g., "Thursday, September 4"
        dateText.getStyleClass().add("hero-date");
        dateRow.getChildren().addAll(dateIcon, dateText);

        Label title = new Label("Your Habits Today");
        title.getStyleClass().add("hero-title");

        HBox subRow = new HBox();
        subRow.getStyleClass().addAll("hero-row");
        subIcon = new Label("\uD83C\uDF05");     // 🌅 (fresh start)
        subIcon.getStyleClass().add("hero-icon");

        subText = new Label("A fresh start awaits! Let's begin!");
        subText.getStyleClass().add("hero-sub");
        subRow.getChildren().addAll(subIcon, subText);

        hero.getChildren().addAll(dateRow, title, subRow);

        return hero;
    }

    // formats to example:  "Thursday, September 4"
    private String formattedDate() {
        java.time.format.DateTimeFormatter fmt =
                java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d");
        return java.time.LocalDate.now().format(fmt);
    }


    /** Helper: toggle header state based on completion */
    public void setCompleted(boolean done) {
        if (hero == null) return; // header not built yet
        var classes = hero.getStyleClass();

        if (done) {
            if (!classes.contains("completed")) classes.add("completed");
            subIcon.setText("\uD83C\uDF89"); // 🎉
            subText.setText("Perfect day! You're crushing it!");
        } else {
            classes.remove("completed");
            subIcon.setText("\uD83C\uDF05"); // 🌅
            subText.setText("A fresh start awaits! Let's begin!");
        }
    }

    /** Optional: call this at midnight or when you refresh the page */
    public void refreshDate() {
        if (dateText != null) dateText.setText(formattedDate());
    }

}


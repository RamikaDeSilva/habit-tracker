package com.ramika.habit.gui;

import com.ramika.habit.model.Category;
import com.ramika.habit.model.Priority;
import com.ramika.habit.service.HabitService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class AddHabitScene {

    private final Stage stage;

    // Fields (accessible later in controller logic)
    private TextField titleField;
    private ComboBox<String> categoryBox;
    private ComboBox<String> priorityBox;
    private ToggleButton mon, tue, wed, thu, fri, sat, sun;

    public AddHabitScene(Stage stage) {
        this.stage = stage;
    }

    /**
     * Build and return the Scene for Add New Habit
     */
    public Scene getScene() {
        VBox root = new VBox(24);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #f7f7fb, #f3f0ff);");

        // Top bar
        HBox header = new HBox(12);
        Button backBtn = new Button("← Back");
        backBtn.setOnAction(e -> goBack());

        Label title = new Label("Add New Habit");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: 700;");

        header.getChildren().addAll(backBtn, title);

        // Card container
        VBox card = new VBox(20);
        card.setPadding(new Insets(24));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 18, 0.12, 0, 6);"
        );

        // Form
        GridPane form = new GridPane();
        form.setVgap(14);
        form.setHgap(14);

        // Habit title
        Label habitTitleLbl = new Label("Habit Title");
        titleField = new TextField();
        titleField.setPromptText("e.g., Morning workout, Read 30 minutes…");

        // Category
        Label categoryLbl = new Label("Category");
        categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Fitness", "Mental Health", "Study", "Lifestyle", "Other");
        categoryBox.getSelectionModel().select("Other");

        // Priority
        Label priorityLbl = new Label("Priority");
        priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("Low", "Medium", "High");
        priorityBox.getSelectionModel().select("Medium");

        // Schedule days
        Label scheduleLbl = new Label("Schedule Days");
        VBox scheduleBox = new VBox(10);

        HBox quickSelect = new HBox(8);
        Button allDaysBtn = new Button("All Days");
        Button weekdaysBtn = new Button("Weekdays");
        Button weekendsBtn = new Button("Weekends");
        Button clearBtn = new Button("Clear");

        quickSelect.getChildren().addAll(allDaysBtn, weekdaysBtn, weekendsBtn, clearBtn);

        FlowPane daysPane = new FlowPane(8, 8);
        mon = new ToggleButton("Mon");
        tue = new ToggleButton("Tue");
        wed = new ToggleButton("Wed");
        thu = new ToggleButton("Thu");
        fri = new ToggleButton("Fri");
        sat = new ToggleButton("Sat");
        sun = new ToggleButton("Sun");
        daysPane.getChildren().addAll(mon, tue, wed, thu, fri, sat, sun);

        scheduleBox.getChildren().addAll(quickSelect, daysPane);

        // Add to form
        form.addRow(0, habitTitleLbl, titleField);
        form.addRow(1, categoryLbl, categoryBox);
        form.addRow(2, priorityLbl, priorityBox);
        form.addRow(3, scheduleLbl, scheduleBox);

        // Buttons
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> goBack());

        Button createBtn = new Button("+ Create Habit");
        createBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #7b61ff, #b06efc);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: 600;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 10 16 10 16;"
        );
        createBtn.setOnAction(e -> handleCreate());

        actions.getChildren().addAll(cancelBtn, createBtn);

        // Assemble card
        card.getChildren().addAll(form, actions);

        // Assemble root
        root.getChildren().addAll(header, card);

        return new Scene(root, 900, 700);
    }

    private void goBack() {
        // For now just print; later you can swap back to dashboard
        Gui gui = new Gui();
        Scene mainScene = gui.createMainScene(stage);
        stage.setScene(mainScene);
        System.out.println("Back clicked → return to dashboard");
    }

    private void handleCreate() {
        String title = titleField.getText().trim();
        String category = categoryBox.getValue();
        String priority = priorityBox.getValue();
        List<String> days = selectedDays();

        //TODO
        // CHANGE INPUTS INTO RESPECTIVE ENUMS / LIST OF DAYS
        EnumSet<DayOfWeek> schedule = EnumSet.noneOf(DayOfWeek.class);

        System.out.println("New Habit Created:");
        System.out.println("Title: " + title);
        System.out.println("Category: " + category);
        System.out.println("Priority: " + priority);
        System.out.println("Days: " + days);


        // TODO: hook this into your data model
        HabitService.createHabit(title, Priority.HIGH, Category.FINANCIAL, schedule);
        goBack();
    }

    private List<String> selectedDays() {
        List<String> days = new ArrayList<>();
        if (mon.isSelected()) days.add("Mon");
        if (tue.isSelected()) days.add("Tue");
        if (wed.isSelected()) days.add("Wed");
        if (thu.isSelected()) days.add("Thu");
        if (fri.isSelected()) days.add("Fri");
        if (sat.isSelected()) days.add("Sat");
        if (sun.isSelected()) days.add("Sun");
        return days;
    }
}


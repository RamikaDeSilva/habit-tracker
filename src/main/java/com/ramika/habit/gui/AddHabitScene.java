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
        categoryBox.getItems().addAll(
                "\uD83C\uDFCB\uFE0F  Fitness",
                "\uD83D\uDCB0  Financial",
                "\uD83E\uDDE0 Mental Health",
                "\uD83D\uDCCB  Other"
        );
        categoryBox.getSelectionModel().select("\uD83D\uDCCB  Other");

        // Priority
        Label priorityLbl = new Label("Priority");
        priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll(
                "\uD83D\uDFE2  Low",
                "\uD83D\uDFE1  Medium",
                "\uD83D\uDD34  High"
        );
        priorityBox.getSelectionModel().select("\uD83D\uDFE1  Medium");

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
        sun = new ToggleButton("Sun");
        mon = new ToggleButton("Mon");
        tue = new ToggleButton("Tue");
        wed = new ToggleButton("Wed");
        thu = new ToggleButton("Thu");
        fri = new ToggleButton("Fri");
        sat = new ToggleButton("Sat");

        daysPane.getChildren().addAll(sun, mon, tue, wed, thu, fri, sat);

        styleGeneralButton(allDaysBtn);
        styleGeneralButton(weekdaysBtn);
        styleGeneralButton(weekendsBtn);
        styleGeneralButton(clearBtn);
        styleDayButton(sun);
        styleDayButton(mon);
        styleDayButton(tue);
        styleDayButton(wed);
        styleDayButton(thu);
        styleDayButton(fri);
        styleDayButton(sat);

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
        Gui gui = new Gui();
        Scene mainScene = gui.createMainScene(stage);
        stage.setScene(mainScene);
        System.out.println("Back clicked → return to dashboard");
    }

    private void handleCreate() {
        String title = titleField.getText() == null ? "" : titleField.getText().trim();

        // 1) Validate title
        if (title.isEmpty()) {
            showWarn("Missing habit name", "Please enter a habit name before creating it.");
            return;
        }

        // 2) Collect selected days, validate at least one
        List<String> days = selectedDays();
        if (days.isEmpty()) {
            showWarn("No days selected", "Choose at least one day for your habit schedule.");
            return;
        }

        // 3) Build rest of the payload
        String category = categoryBox.getValue();
        Category categoryValue = validateCategory(category);

        String priority = priorityBox.getValue();
        Priority priorityValue = validatePriority(priority);

        // 4) Convert day labels -> EnumSet<DayOfWeek>
        EnumSet<DayOfWeek> schedule = EnumSet.noneOf(DayOfWeek.class);
        for (String day : days) {
            switch (day) {
                case "Sun" -> schedule.add(DayOfWeek.SUNDAY);
                case "Mon" -> schedule.add(DayOfWeek.MONDAY);
                case "Tue" -> schedule.add(DayOfWeek.TUESDAY);
                case "Wed" -> schedule.add(DayOfWeek.WEDNESDAY);
                case "Thu" -> schedule.add(DayOfWeek.THURSDAY);
                case "Fri" -> schedule.add(DayOfWeek.FRIDAY);
                case "Sat" -> schedule.add(DayOfWeek.SATURDAY);
            }
        }

        // 5) Create and go back
        HabitService.createHabit(title, priorityValue, categoryValue, schedule);
        goBack();
    }

    /** Show a simple warning popup (matches your dialog flow). */
    private void showWarn(String header, String content) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Validation");
        a.setHeaderText(header);
        a.setContentText(content);
        a.getButtonTypes().setAll(ButtonType.OK);
        a.showAndWait();
    }



    private List<String> selectedDays() {
        List<String> days = new ArrayList<>();
        if (sun.isSelected()) days.add("Sun");
        if (mon.isSelected()) days.add("Mon");
        if (tue.isSelected()) days.add("Tue");
        if (wed.isSelected()) days.add("Wed");
        if (thu.isSelected()) days.add("Thu");
        if (fri.isSelected()) days.add("Fri");
        if (sat.isSelected()) days.add("Sat");
        return days;
    }

    // --- Style Helpers ---
    private void styleDayButton(ToggleButton button) {
        button.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                button.setStyle("-fx-background-color: #7b61ff; -fx-text-fill: white;");
            } else {
                button.setStyle(""); // Reset to default
            }
        });
    }

    private void styleGeneralButton(Button button) {
        button.setStyle(
                "-fx-background-color: linear-gradient(to right, #7b61ff, #b06efc);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: 600;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 10 16 10 16;"
        );
        switch (button.getText()) {
            case "All Days" -> button.setOnAction(e -> {
                mon.setSelected(true);
                tue.setSelected(true);
                wed.setSelected(true);
                thu.setSelected(true);
                fri.setSelected(true);
                sat.setSelected(true);
                sun.setSelected(true);
            });
            case "Weekdays" -> button.setOnAction(e -> {
                mon.setSelected(true);
                tue.setSelected(true);
                wed.setSelected(true);
                thu.setSelected(true);
                fri.setSelected(true);
                sat.setSelected(false);
                sun.setSelected(false);
            });
            case "Weekends" -> button.setOnAction(e -> {
                mon.setSelected(false);
                tue.setSelected(false);
                wed.setSelected(false);
                thu.setSelected(false);
                fri.setSelected(false);
                sat.setSelected(true);
                sun.setSelected(true);
            });
            case "Clear" -> button.setOnAction(e -> {
                mon.setSelected(false);
                tue.setSelected(false);
                wed.setSelected(false);
                thu.setSelected(false);
                fri.setSelected(false);
                sat.setSelected(false);
                sun.setSelected(false);
            });
            default -> {}
        }
    }

    // --- Normalization + Validation ---
    private static String normalizeLabel(String s) {
        if (s == null) return "";
        String core = s.replaceAll("[^A-Za-z ]", "").trim().replaceAll("\\s+", " ");
        return core;
    }

    private Priority validatePriority(String priority) {
        String core = normalizeLabel(priority).toLowerCase();
        return switch (core) {
            case "low"    -> Priority.LOW;
            case "high"   -> Priority.HIGH;
            case "medium" -> Priority.MEDIUM;
            default       -> Priority.MEDIUM;
        };
    }

    private Category validateCategory(String category) {
        String core = normalizeLabel(category).toLowerCase();
        return switch (core) {
            case "fitness"       -> Category.FITNESS;
            case "financial"     -> Category.FINANCIAL;
            case "mental health" -> Category.MENTALHEALTH;
            case "other"         -> Category.OTHER;
            default              -> Category.OTHER;
        };
    }

}

package com.ramika.habit.gui;

import com.ramika.habit.exceptions.HabitAlreadyCompleteException;
import com.ramika.habit.exceptions.HabitNotActiveTodayException;
import com.ramika.habit.exceptions.HabitNotFoundException;
import com.ramika.habit.model.Habit;
import com.ramika.habit.service.HabitService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.UUID;

public class HabitCard extends StackPane {

    private final CheckBox checkBox = new CheckBox();
    private final Label    titleLbl;
    private final Button   deleteBtn;
    private final HBox     daysRow = new HBox(6);

    private UUID    habitId;
    private boolean activeToday;
    private boolean updatingFromService = false; // prevent loops

    public HabitCard(String emoji, String title, String frequencyText) {
        // Card surface
        setPadding(new Insets(18));
        setBackground(new Background(new BackgroundFill(Color.web("#FCFCFF"), new CornerRadii(20), Insets.EMPTY)));
        setEffect(new DropShadow(12, Color.rgb(0, 0, 0, 0.12)));
        setMaxWidth(850);
        getStyleClass().add("habit-card");

        // --- Checkbox (real JavaFX control) ---
        checkBox.getStyleClass().add("habit-checkbox");
        checkBox.setFocusTraversable(false);
        checkBox.setCursor(Cursor.HAND);

        // Text column
        titleLbl = new Label(emoji + "  " + title);
        titleLbl.setStyle("-fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: #111827;");

        VBox textCol = new VBox(10, titleLbl, daysRow);
        textCol.setAlignment(Pos.CENTER_LEFT);

        HBox row = new HBox(12, checkBox, textCol);
        row.setAlignment(Pos.CENTER_LEFT);

        // --- Delete button (top-right corner) ---
        deleteBtn = new Button("✕");
        deleteBtn.getStyleClass().add("habit-delete-btn");
        deleteBtn.setFocusTraversable(false);
        deleteBtn.setCursor(Cursor.HAND);
        deleteBtn.setOnAction(e -> confirmAndDelete());
        Tooltip.install(deleteBtn, new Tooltip("Delete habit"));

        // Add row as main content, delete button overlaid top-right
        getChildren().addAll(row, deleteBtn);
        StackPane.setAlignment(deleteBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(deleteBtn, new Insets(6, 8, 0, 0));

        // Right-click: delete (still available)
        MenuItem delete = new MenuItem("Delete habit");
        delete.setOnAction(e -> confirmAndDelete());
        ContextMenu menu = new ContextMenu(delete);
        setOnContextMenuRequested(e -> menu.show(this, e.getScreenX(), e.getScreenY()));
    }

    /** Connect this card to a Habit and wire service calls. */
    public void bindToHabit(Habit habit) {
        this.habitId = habit.getId();

        // Is this habit scheduled for TODAY?
        DayOfWeek todayDow = LocalDate.now().getDayOfWeek();
        this.activeToday = habit.getSchedule() != null && habit.getSchedule().contains(todayDow);

        // Initial state
        boolean doneToday = activeToday && habit.isCompletedOn(LocalDate.now());
        checkBox.setSelected(doneToday);

        // Draw the weekly schedule row
        updateDayChips(habit.getSchedule());

        if (!activeToday) {
            getStyleClass().add("habit-card-inactive");
            setOpacity(0.55);
            checkBox.setDisable(true);
            checkBox.setCursor(Cursor.DEFAULT);
            Tooltip.install(this, new Tooltip("Not scheduled for today"));
        } else {
            checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (updatingFromService) return;
                try {
                    HabitService.setCompletedToday(habitId, newVal);
                } catch (HabitNotFoundException ex) {
                    showInfo("Habit not found.");
                    revert();
                } catch (HabitNotActiveTodayException ex) {
                    showInfo("This habit isn’t scheduled for today.");
                    revert();
                } catch (HabitAlreadyCompleteException ex) {
                    checkBox.setSelected(true);
                } catch (Exception ex) {
                    showInfo("Couldn’t update. Please try again.");
                    revert();
                }
            });
        }
    }

    /** Draw the row of day chips (Sun → Sat). */
    private void updateDayChips(EnumSet<DayOfWeek> schedule) {
        daysRow.getChildren().clear();
        if (schedule == null) return;

        DayOfWeek[] order = {
                DayOfWeek.SUNDAY,
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY
        };
        String[] labels = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

        DayOfWeek today = LocalDate.now().getDayOfWeek();

        for (int i = 0; i < order.length; i++) {
            DayOfWeek dow = order[i];
            Label chip = new Label(labels[i]);
            chip.setPadding(new Insets(4, 10, 4, 10));

            if (schedule.contains(dow)) {
                chip.getStyleClass().setAll("day-chip", "active");
            } else {
                chip.getStyleClass().setAll("day-chip", "inactive");
            }

            // Highlight today's chip with a bold border
            if (dow == today) {
                chip.setStyle(chip.getStyle() +
                        "; -fx-border-color: #333333; -fx-border-width: 1.5px; -fx-border-radius: 20px;");
            }

            daysRow.getChildren().add(chip);
        }
    }

    /** Let external code programmatically reflect today's completion. */
    public void setCompletedToday(boolean completed) {
        updatingFromService = true;
        checkBox.setSelected(completed);
        updatingFromService = false;
    }

    private void revert() {
        updatingFromService = true;
        checkBox.setSelected(!checkBox.isSelected());
        updatingFromService = false;
    }

    private void confirmAndDelete() {
        if (habitId == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Habit");
        alert.setHeaderText(null);

        Label msg = new Label("Are you sure you want to delete this habit ");
        Label perm = new Label("permanently");
        perm.setStyle("-fx-font-weight: bold;");
        Label q = new Label("?");

        HBox content = new HBox(2, msg, perm, q);
        alert.getDialogPane().setContent(content);

        ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yes, cancel);

        alert.showAndWait().ifPresent(btn -> {
            if (btn == yes) {
                try {
                    HabitService.removeHabit(habitId);
                } catch (HabitNotFoundException e) {
                    showInfo("Habit already removed.");
                } catch (Exception e) {
                    showInfo("Couldn’t remove habit.");
                }
            }
        });
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

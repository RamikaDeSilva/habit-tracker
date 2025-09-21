package com.ramika.habit.gui;

import com.ramika.habit.exceptions.HabitAlreadyCompleteException;
import com.ramika.habit.exceptions.HabitNotActiveTodayException;
import com.ramika.habit.exceptions.HabitNotFoundException;
import com.ramika.habit.model.Habit;
import com.ramika.habit.service.HabitService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.UUID;

public class HabitCard extends StackPane {

    private final CheckBox checkBox = new CheckBox();
    private final Label    titleLbl;
    private final Label    freqLbl;

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
        checkBox.setFocusTraversable(false);     // no blue focus ring
        checkBox.setCursor(Cursor.HAND);

        // Text column
        titleLbl = new Label(emoji + "  " + title);
        titleLbl.setStyle("-fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: #111827;");

        freqLbl  = new Label("📅  " + frequencyText);
        freqLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280; -fx-font-weight: 600;");

        VBox textCol = new VBox(6, titleLbl, freqLbl);
        textCol.setAlignment(Pos.CENTER_LEFT);

        HBox row = new HBox(12, checkBox, textCol);
        row.setAlignment(Pos.CENTER_LEFT);

        getChildren().add(row);

        // Right-click: delete
        MenuItem delete = new MenuItem("Delete habit");
        delete.setOnAction(e -> tryRemove());
        ContextMenu menu = new ContextMenu(delete);
        setOnContextMenuRequested(e -> menu.show(this, e.getScreenX(), e.getScreenY()));
    }

    /** Connect this card to a Habit and wire service calls. */
    public void bindToHabit(Habit habit) {
        this.habitId = habit.getId();

        // Is this habit scheduled for TODAY?
        DayOfWeek todayDow = LocalDate.now().getDayOfWeek();
        this.activeToday = habit.getSchedule() != null && habit.getSchedule().contains(todayDow);

        // Initial state (only “complete today” makes sense if active today)
        boolean doneToday = activeToday && habit.isCompletedOn(LocalDate.now());
        checkBox.setSelected(doneToday);

        if (!activeToday) {
            // Grey out + disable interaction
            getStyleClass().add("habit-card-inactive");
            setOpacity(0.55);
            checkBox.setDisable(true);
            checkBox.setCursor(Cursor.DEFAULT);
            Tooltip.install(this, new Tooltip("Not scheduled for today"));
        } else {
            // When user clicks, ask service to set today's completion
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
                    // benign; keep selected
                    checkBox.setSelected(true);
                } catch (Exception ex) {
                    showInfo("Couldn’t update. Please try again.");
                    revert();
                }
            });
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

    private void tryRemove() {
        if (habitId == null) return;
        try {
            HabitService.removeHabit(habitId);
        } catch (HabitNotFoundException e) {
            showInfo("Habit already removed.");
        } catch (Exception e) {
            showInfo("Couldn’t remove habit.");
        }
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

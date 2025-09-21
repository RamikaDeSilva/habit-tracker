package com.ramika.habit.gui;

import com.ramika.habit.exceptions.HabitAlreadyCompleteException;
import com.ramika.habit.exceptions.HabitNotActiveTodayException;
import com.ramika.habit.exceptions.HabitNotFoundException;
import com.ramika.habit.model.Habit;
import com.ramika.habit.service.HabitService;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.UUID;

public class HabitCard extends StackPane {
    private final BooleanProperty completed = new SimpleBooleanProperty(false);

    private final StackPane checkbox;   // left box we click
    private final Rectangle box;        // the square outline/background
    private final Label check;          // the ✓ label
    private final StackPane greenFill;  // the green fill when complete

    private UUID habitId;               // bound habit id for service calls
    private boolean activeToday;        // whether this habit is scheduled today

    public HabitCard(String emoji, String title, String frequencyText) {
        // card surface
        setPadding(new Insets(18));
        setBackground(new Background(new BackgroundFill(Color.web("#FCFCFF"), new CornerRadii(20), Insets.EMPTY)));
        setEffect(new DropShadow(12, Color.rgb(0, 0, 0, 0.12)));
        setMaxWidth(850);

        // left checkbox (tall style like your design)
        box = new Rectangle(28, 75);
        box.setArcWidth(10);
        box.setArcHeight(10);
        box.setFill(Color.WHITE);
        box.setStroke(Color.web("#D1D5DB"));
        box.setStrokeWidth(1.5);

        check = new Label("✓");
        check.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        check.setVisible(false);

        greenFill = new StackPane(check);
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

        // visual reaction when 'completed' changes
        completed.addListener((obs, was, is) -> {
            greenFill.setVisible(is);
            check.setVisible(is);
            box.setStroke(is ? Color.web("#16a34a") : Color.web("#D1D5DB"));
        });

        // (Optional) right-click delete menu
        MenuItem delete = new MenuItem("Delete habit");
        delete.setOnAction(e -> tryRemove());
        ContextMenu menu = new ContextMenu(delete);
        setOnContextMenuRequested(e -> menu.show(this, e.getScreenX(), e.getScreenY()));
    }

    // -------- public API you already use --------
    public BooleanProperty completedProperty() { return completed; }
    public boolean isCompleted() { return completed.get(); }
    public void setCompleted(boolean value) { completed.set(value); }

    // -------- new binding to the model/service --------
    /** Connect this card’s UI to a Habit model and wire service actions. */
    public void bindToHabit(Habit habit) {
        this.habitId = habit.getId();

        // Is this habit scheduled for TODAY?
        DayOfWeek todayDow = LocalDate.now().getDayOfWeek();
        this.activeToday = habit.getSchedule() != null && habit.getSchedule().contains(todayDow);

        // initialize checkbox from today’s state if active; else force unchecked
        boolean doneToday = activeToday && habit.isCompletedOn(LocalDate.now());
        setCompleted(doneToday);

        // If NOT active today → grey out + disable interactions
        if (!activeToday) {
            getStyleClass().add("habit-card-inactive"); // CSS hook
            setOpacity(0.55);
            checkbox.setDisable(true);
            checkbox.setCursor(Cursor.DEFAULT);
            Tooltip.install(this, new Tooltip("Not scheduled for today"));
            // Also ensure clicks don’t toggle accidentally:
            checkbox.setOnMouseClicked(e -> e.consume());
            return; // don't attach toggle handler
        }

        // If active today → clicking the box toggles via service
        checkbox.setOnMouseClicked(e -> {
            boolean target = !isCompleted(); // desired state after click
            try {
                HabitService.setCompletedToday(habitId, target);
                setCompleted(target); // reflect success
            } catch (HabitNotFoundException ex) {
                setCompleted(!target);
                showInfo("Habit not found.");
            } catch (HabitNotActiveTodayException ex) {
                setCompleted(!target);
                showInfo("This habit isn’t scheduled for today.");
            } catch (HabitAlreadyCompleteException ex) {
                // benign in your model; keep UI consistent
                setCompleted(true);
            } catch (Exception ex) {
                setCompleted(!target);
                showInfo("Couldn’t update. Please try again.");
            }
        });
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

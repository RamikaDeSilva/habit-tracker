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
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.UUID;

public class HabitCard extends StackPane {
    private final BooleanProperty completed = new SimpleBooleanProperty(false);

    private final StackPane checkbox;   // left box we click
    private final Rectangle box;        // the square outline/background
    private final Label check;          // the ✓ label
    private final StackPane greenFill;  // the green fill when complete

    private final Button deleteBtn;     // small "×" button on the right

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

        // --- delete button (right) ---
        deleteBtn = new Button("✕");
        deleteBtn.getStyleClass().add("habit-delete-btn");
        deleteBtn.setCursor(Cursor.HAND);
        deleteBtn.setFocusTraversable(false);
        deleteBtn.setOnAction(e -> confirmAndDelete());
        Tooltip.install(deleteBtn, new Tooltip("Delete habit"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(12, checkbox, textCol, spacer, deleteBtn);
        row.setAlignment(Pos.CENTER_LEFT);

        getChildren().add(row);

        // visual reaction when 'completed' changes
        completed.addListener((obs, was, is) -> {
            greenFill.setVisible(is);
            check.setVisible(is);
            box.setStroke(is ? Color.web("#16a34a") : Color.web("#D1D5DB"));
        });

        // Optional context menu delete (kept)
        MenuItem delete = new MenuItem("Delete habit");
        delete.setOnAction(e -> confirmAndDelete());
        ContextMenu menu = new ContextMenu(delete);
        setOnContextMenuRequested(e -> menu.show(this, e.getScreenX(), e.getScreenY()));
    }

    // -------- public API you already use --------
    public BooleanProperty completedProperty() { return completed; }
    public boolean isCompleted() { return completed.get(); }
    public void setCompleted(boolean value) { completed.set(value); }

    // -------- binding to the model/service --------
    /** Connect this card’s UI to a Habit model and wire service actions. */
    public void bindToHabit(Habit habit) {
        this.habitId = habit.getId();

        // Is this habit scheduled for TODAY?
        DayOfWeek todayDow = LocalDate.now().getDayOfWeek();
        this.activeToday = habit.getSchedule() != null && habit.getSchedule().contains(todayDow);

        // initialize checkbox from today’s state if active; else force unchecked
        boolean doneToday = activeToday && habit.isCompletedOn(LocalDate.now());
        setCompleted(doneToday);

        // If NOT active today → grey out + disable interactions (but keep delete active)
        if (!activeToday) {
            getStyleClass().add("habit-card-inactive"); // CSS hook
            setOpacity(0.55);
            checkbox.setDisable(true);
            checkbox.setCursor(Cursor.DEFAULT);
            Tooltip.install(this, new Tooltip("Not scheduled for today"));
            checkbox.setOnMouseClicked(e -> e.consume());
        } else {
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
                    setCompleted(true); // benign; keep consistent
                } catch (Exception ex) {
                    setCompleted(!target);
                    showInfo("Couldn’t update. Please try again.");
                }
            });
        }
    }

    // ---- deletion flow ----
    private void confirmAndDelete() {
        if (habitId == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Habit");
        alert.setHeaderText(null);

        // Build “Are you sure you want to delete this habit PERMANENTLY?” with bold permanently
        Text t1 = new Text("Are you sure you want to delete this habit ");
        Text t2 = new Text("permanently");
        t2.setStyle("-fx-font-weight: bold;");
        Text t3 = new Text("?");
        TextFlow flow = new TextFlow(t1, t2, t3);

        DialogPane pane = alert.getDialogPane();
        pane.setContent(flow);

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

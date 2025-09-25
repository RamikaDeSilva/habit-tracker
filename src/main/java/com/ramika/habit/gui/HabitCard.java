package com.ramika.habit.gui;

import com.ramika.habit.exceptions.HabitAlreadyCompleteException;
import com.ramika.habit.exceptions.HabitNotActiveTodayException;
import com.ramika.habit.exceptions.HabitNotFoundException;
import com.ramika.habit.model.Category;
import com.ramika.habit.model.Habit;
import com.ramika.habit.model.Priority;
import com.ramika.habit.service.HabitService;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.UUID;

public class HabitCard extends StackPane {

    private final CheckBox checkBox = new CheckBox();
    private final Label    titleLbl;
    private final Button   deleteBtn;

    private final HBox     daysRow = new HBox(6);

    // badges row for category + priority
    private final HBox     badgesRow = new HBox(8);
    private final Label    categoryChip = new Label();
    private final Label    priorityChip = new Label();

    private UUID    habitId;
    private boolean activeToday;
    private boolean updatingFromService = false; // prevent loops

    // hover animation bits
    private final DropShadow baseShadow = new DropShadow(12, Color.rgb(0, 0, 0, 0.12));
    private final DropShadow hoverShadow = new DropShadow(20, Color.rgb(15, 23, 42, 0.25));
    private Timeline hoverInTl;
    private Timeline hoverOutTl;

    public HabitCard(String emoji, String title, String frequencyText) {
        // Card surface
        setPadding(new Insets(18));
        setBackground(new Background(new BackgroundFill(Color.web("#FCFCFF"), new CornerRadii(20), Insets.EMPTY)));
        setEffect(baseShadow);
        setMaxWidth(850);
        getStyleClass().add("habit-card");

        // Prepare hover shadow (start with same params as base, animate later)
        hoverShadow.setRadius(24);
        hoverShadow.setOffsetY(4);
        hoverShadow.setSpread(0.12);

        // --- Checkbox (real JavaFX control) ---
        checkBox.getStyleClass().add("habit-checkbox");
        checkBox.setFocusTraversable(false);
        checkBox.setCursor(Cursor.HAND);

        // Text column (emoji and title split into two labels so emoji uses emoji-capable font)
        Label emojiLbl = new Label(emoji);
        emojiLbl.setStyle(
                "-fx-font-family: 'Apple Color Emoji','Segoe UI Emoji','Noto Color Emoji','Segoe UI Symbol';" +
                        "-fx-font-size: 22px;" +
                        "-fx-label-padding: 0 6 0 0;"
        );

        titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: #111827;");

        HBox titleRow = new HBox(6, emojiLbl, titleLbl);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        // badges
        styleChipBase(categoryChip);
        styleChipBase(priorityChip);
        badgesRow.setAlignment(Pos.CENTER_LEFT);
        badgesRow.getChildren().addAll(categoryChip, priorityChip);

        // spacing: tight title↔badges, more space before days
        VBox headerCol = new VBox(4, titleRow, badgesRow); // 4px gap
        headerCol.setAlignment(Pos.CENTER_LEFT);

        VBox textCol = new VBox(14, headerCol, daysRow);   // 14px gap
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

        // Context menu: delete
        MenuItem delete = new MenuItem("Delete habit");
        delete.setOnAction(e -> confirmAndDelete());
        ContextMenu menu = new ContextMenu(delete);
        setOnContextMenuRequested(e -> menu.show(this, e.getScreenX(), e.getScreenY()));

        // --- Hover animation setup ---
        setupHoverAnimation();
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

        // badges & days
        updateBadges(habit);
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
                DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
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

    // ===== badges =====

    private void updateBadges(Habit habit) {
        // Category chip
        Category cat = habit.getCategory();
        if (cat == null) cat = Category.OTHER;
        switch (cat) {
            case FITNESS      -> setChip(categoryChip, "🏋️  Fitness",       "#fff6e5", "#b35300");
            case FINANCIAL    -> setChip(categoryChip, "💰  Financial",     "#e9fff1", "#0b6b3e");
            case MENTALHEALTH -> setChip(categoryChip, "🧠  Mental Health", "#eef2ff", "#4338ca");
            case OTHER        -> setChip(categoryChip, "📋  Other",          "#f3f4f6", "#374151");
        }

        // Priority chip
        Priority pr = habit.getPriority();
        if (pr == null) pr = Priority.MEDIUM;
        switch (pr) {
            case LOW    -> setChip(priorityChip, "🟢  Low",    "#eafff1", "#0d8a4e");
            case MEDIUM -> setChip(priorityChip, "🟡  Medium", "#fffbe6", "#8a6d00");
            case HIGH   -> setChip(priorityChip, "🔴  High",   "#ffecec", "#a60202");
        }
    }

    private void styleChipBase(Label chip) {
        chip.setPadding(new Insets(4, 10, 4, 10));
        chip.setStyle("-fx-background-radius: 14; -fx-font-size: 12px; -fx-font-weight: 600;");
    }

    private void setChip(Label chip, String text, String bgHex, String fgHex) {
        chip.setText(text);
        chip.setStyle(
                "-fx-background-radius: 14;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: 600;" +
                        "-fx-background-color: " + bgHex + ";" +
                        "-fx-text-fill: " + fgHex + ";"
        );
    }

    // ===== hover animations =====

    private void setupHoverAnimation() {
        // ensure transform origin looks good
        setScaleX(1.0);
        setScaleY(1.0);
        setTranslateY(0);

        // Animate to "lifted" state
        hoverInTl = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(scaleXProperty(),  getScaleX()),
                        new KeyValue(scaleYProperty(),  getScaleY()),
                        new KeyValue(translateYProperty(), getTranslateY()),
                        new KeyValue(baseShadow.radiusProperty(), baseShadow.getRadius()),
                        new KeyValue(baseShadow.offsetYProperty(), baseShadow.getOffsetY() == 0 ? 2.0 : baseShadow.getOffsetY()),
                        new KeyValue(baseShadow.spreadProperty(), 0.08)
                ),
                new KeyFrame(Duration.millis(160),
                        new KeyValue(scaleXProperty(), 1.025, Interpolator.EASE_BOTH),
                        new KeyValue(scaleYProperty(), 1.025, Interpolator.EASE_BOTH),
                        new KeyValue(translateYProperty(), -2, Interpolator.EASE_BOTH),
                        new KeyValue(baseShadow.radiusProperty(), 24, Interpolator.EASE_BOTH),
                        new KeyValue(baseShadow.offsetYProperty(), 4, Interpolator.EASE_BOTH),
                        new KeyValue(baseShadow.spreadProperty(), 0.18, Interpolator.EASE_BOTH)
                )
        );

        // Animate back to base state
        hoverOutTl = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(scaleXProperty(),  getScaleX()),
                        new KeyValue(scaleYProperty(),  getScaleY()),
                        new KeyValue(translateYProperty(), getTranslateY()),
                        new KeyValue(baseShadow.radiusProperty(), baseShadow.getRadius()),
                        new KeyValue(baseShadow.offsetYProperty(), baseShadow.getOffsetY()),
                        new KeyValue(baseShadow.spreadProperty(), baseShadow.getSpread())
                ),
                new KeyFrame(Duration.millis(160),
                        new KeyValue(scaleXProperty(), 1.0, Interpolator.EASE_BOTH),
                        new KeyValue(scaleYProperty(), 1.0, Interpolator.EASE_BOTH),
                        new KeyValue(translateYProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(baseShadow.radiusProperty(), 12, Interpolator.EASE_BOTH),
                        new KeyValue(baseShadow.offsetYProperty(), 2, Interpolator.EASE_BOTH),
                        new KeyValue(baseShadow.spreadProperty(), 0.12, Interpolator.EASE_BOTH)
                )
        );

        // Handlers
        setOnMouseEntered(e -> {
            setCursor(Cursor.HAND);
            hoverOutTl.stop();
            hoverInTl.playFromStart();
        });
        setOnMouseExited(e -> {
            setCursor(Cursor.DEFAULT);
            hoverInTl.stop();
            hoverOutTl.playFromStart();
        });
    }
}

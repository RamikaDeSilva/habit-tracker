package main.java.com.ramika.habit.model;

import java.time.LocalDate;

public class HabitCompletion {
    private LocalDate date;
    private boolean completed;

    public HabitCompletion(LocalDate date, boolean completed) {
        this.date = date;
        this.completed = completed;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public String toString() {
        return date.toString();
    }
}

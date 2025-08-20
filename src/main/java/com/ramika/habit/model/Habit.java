package main.java.com.ramika.habit.model;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.UUID;

// represents a Habit with a unique id, a name, priority level,
// category and active status
public class Habit {
    private final UUID id;
    private String name;
    private Priority priority;
    private Category category;
    private Status activeStatus;

    private EnumSet<DayOfWeek> schedule;

    // Creates a new habit with given name, priority, category, and activeStatus set to Active
    public Habit(UUID id, String name, Priority priority, Category category, EnumSet<DayOfWeek> schedule) {
        this.id = id;
        this.name = name;
        this.priority = priority;
        this.category = category;
        this.activeStatus = Status.ACTIVE;
        this.schedule = schedule;
    }

    public UUID getId() {
        return id;
    }

    public EnumSet<DayOfWeek> getSchedule() {
        return schedule;
    }

    public void setSchedule(EnumSet<DayOfWeek> schedule) {
        this.schedule = schedule;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Status getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(Status activeStatus) {
        this.activeStatus = activeStatus;
    }
}

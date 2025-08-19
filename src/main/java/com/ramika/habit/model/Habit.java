package main.java.com.ramika.habit.model;

import java.util.UUID;

// represents a Habit with a unique id, a name, priority level,
// category and active status
public class Habit {
    private UUID id;
    private String name;
    private Priority priority;
    private Category category;
    private Status activeStatus;

    // Creates a new habit with given name, priority, category, and activeStatus set to Active
    public Habit(String name, Priority priority, Category category) {
        id = UUID.randomUUID();
        this.name = name;
        this.priority = priority;
        this.category = category;
        this.activeStatus = Status.ACTIVE;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

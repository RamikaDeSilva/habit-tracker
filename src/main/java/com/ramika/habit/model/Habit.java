package com.ramika.habit.model;

import com.ramika.habit.exceptions.HabitAlreadyCompleteException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import java.time.LocalDate;

// represents a Habit with a unique id, a name, priority level,
// category and active status
public class Habit {
    private final UUID id;
    private String name;
    private Priority priority;
    private Category category;
    private Status activeStatus;
    private EnumSet<DayOfWeek> schedule;
    private List<HabitCompletion> completions = new ArrayList<>();

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

    public List<HabitCompletion> getCompletions() {
        return completions;
    }

    public void markCompletedToday() throws HabitAlreadyCompleteException {
        LocalDate today = LocalDate.now();
//        HabitCompletion completion = new HabitCompletion(today, true);
        if (findCompletionByDate(today) == null) {
            HabitCompletion completion = new HabitCompletion(today, true);
            completions.add(completion);
        } else {
            throw new HabitAlreadyCompleteException();
        }
//        completions.add(completion);
    }

//    public void addCompletion(HabitCompletion completion) {
//        completions.add(completion);
//    }

    // Update a completion by index
    public void updateCompletion(int index, HabitCompletion updatedCompletion) {
        if (index >= 0 && index < completions.size()) {
            completions.set(index, updatedCompletion);
        }
    }

    // Remove a completion by index
    public void removeCompletion(int index) {
        if (index >= 0 && index < completions.size()) {
            completions.remove(index);
        }
    }

    // Find a completion by date (example)
    public HabitCompletion findCompletionByDate(LocalDate date) {
        for (HabitCompletion hc : completions) {
            if (hc.getDate().equals(date)) {
                return hc;
            }
        }
        return null;
    }


    public boolean isCompletedOn(LocalDate date) {
        HabitCompletion hc = findCompletionByDate(date);
        return hc != null && hc.isCompleted();  // assumes HabitCompletion has isCompleted()
    }

    public boolean isCompletedToday() {
        return isCompletedOn(LocalDate.now());
    }

    public void unmarkCompletedOn(LocalDate date) {
        // simplest: remove the entry for that day
        completions.removeIf(hc -> hc.getDate().equals(date));
    }

    public void unmarkCompletedToday() {
        unmarkCompletedOn(LocalDate.now());
    }

    // sets completion status for a given date
    public void setCompletedOn(LocalDate date, boolean completed) throws HabitAlreadyCompleteException {
        if (completed) {
            // reuse your existing guard
            if (findCompletionByDate(date) == null) {
                completions.add(new HabitCompletion(date, true));
            } else {
                // already completed → keep as is or throw if you want strictness
                // throw new HabitAlreadyCompleteException();
            }
        } else {
            unmarkCompletedOn(date);
        }
    }


}

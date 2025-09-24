package com.ramika.habit.service;

import com.ramika.habit.exceptions.AlreadyNotActiveException;
import com.ramika.habit.exceptions.HabitAlreadyCompleteException;
import com.ramika.habit.exceptions.HabitNotActiveTodayException;
import com.ramika.habit.exceptions.HabitNotFoundException;
import com.ramika.habit.model.Category;
import com.ramika.habit.model.Habit;
import com.ramika.habit.model.Priority;
import com.ramika.habit.model.Status;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class HabitService {
    private static HabitService instance;
    private static final Map<UUID, Habit> allHabits    = new LinkedHashMap<>();
    private static final Map<UUID, Habit> activeHabits = new LinkedHashMap<>();

    private static final IntegerProperty totalDisplayed     = new SimpleIntegerProperty(0);
    private static final IntegerProperty completedDisplayed = new SimpleIntegerProperty(0);
    private static final DoubleProperty  percentDisplayed   = new SimpleDoubleProperty(0.0);

    private HabitService() {}
    public static HabitService getInstance() { if (instance != null) return instance; instance = new HabitService(); return instance; }

    public static Map<UUID, Habit> getAllHabits()    { return allHabits; }
    public static Map<UUID, Habit> getActiveHabits() { return activeHabits; }

    public static void forceRecompute() { recomputeDashboardMetrics(); }

    private static void recomputeDashboardMetrics() {
        LocalDate today = LocalDate.now();
        DayOfWeek dow   = today.getDayOfWeek();

        int total = 0;
        int completedToday = 0;

        for (Habit h : activeHabits.values()) {
            if (h.getSchedule() != null && h.getSchedule().contains(dow)) {
                total++;
                if (h.isCompletedOn(today)) {
                    completedToday++;
                }
            }
        }

        totalDisplayed.set(total);
        completedDisplayed.set(completedToday);
        percentDisplayed.set(total == 0 ? 1.0 : (double) completedToday / (double) total);
    }

    public static ReadOnlyIntegerProperty totalDisplayedProperty()     { return totalDisplayed; }
    public static ReadOnlyIntegerProperty completedDisplayedProperty() { return completedDisplayed; }
    public static ReadOnlyDoubleProperty  percentDisplayedProperty()   { return percentDisplayed; }

    public static void createHabitFromPersistence(UUID id, String name, Priority priority,
                                                  Category category, EnumSet<DayOfWeek> schedule,
                                                  Status status) {
        Habit habit = new Habit(id, name, priority, category, schedule);
        allHabits.put(id, habit);
        if (status == Status.ACTIVE) {
            activeHabits.put(id, habit);
        } else {
            habit.setActiveStatus(Status.INACTIVE);
        }
        recomputeDashboardMetrics();
    }

    public static UUID validIdExist(UUID searchId, String habitName) {
        for (UUID id : allHabits.keySet()) {
            if (allHabits.get(id) != null) {
                String name = allHabits.get(id).getName().toLowerCase();
                if (name.equals(habitName)) searchId = id;
            }
        }
        return searchId;
    }

    public static void createHabit(String name, Priority priority, Category category, EnumSet<DayOfWeek> schedule) {
        UUID id = UUID.randomUUID();
        Habit habit = new Habit(id, name, priority, category, schedule);
        allHabits.put(id, habit);
        activeHabits.put(id, habit);
        recomputeDashboardMetrics();
        Persistence.saveSnapshot();
    }

    public static void removeHabit(UUID habitID) throws HabitNotFoundException {
        if (!allHabits.containsKey(habitID)) throw new HabitNotFoundException();

        Habit removedHabit = allHabits.get(habitID);
        if (removedHabit.getActiveStatus() == Status.ACTIVE) {
            if (!activeHabits.containsKey(habitID)) throw new HabitNotFoundException();
            activeHabits.remove(habitID);
        }
        allHabits.remove(habitID);
        recomputeDashboardMetrics();
        Persistence.saveSnapshot();
    }

    public static void deactivateHabit(UUID habitID) throws HabitNotFoundException, AlreadyNotActiveException {
        if (!allHabits.containsKey(habitID)) throw new HabitNotFoundException();

        Habit deactivatedHabit = allHabits.get(habitID);
        if (deactivatedHabit.getActiveStatus() == Status.ACTIVE) {
            deactivatedHabit.setActiveStatus(Status.INACTIVE);
            activeHabits.remove(habitID);
            recomputeDashboardMetrics();
            Persistence.saveSnapshot();
        } else {
            throw new AlreadyNotActiveException();
        }
    }

    public static void changeName(UUID searchId, String name) {
        Habit h = allHabits.get(searchId);
        if (h != null) h.setName(name);
        recomputeDashboardMetrics();
        Persistence.saveSnapshot();
    }

    public static void changePriority(UUID searchId, Priority priority) {
        Habit h = allHabits.get(searchId);
        if (h != null) h.setPriority(priority);
        recomputeDashboardMetrics();
        Persistence.saveSnapshot();
    }

    public static void changeCategory(UUID searchId, Category category) {
        Habit h = allHabits.get(searchId);
        if (h != null) h.setCategory(category);
        recomputeDashboardMetrics();
        Persistence.saveSnapshot();
    }

    // Mark completed for TODAY
    public static void markHabitCompletedToday(UUID habitId)
            throws HabitNotFoundException, HabitAlreadyCompleteException, HabitNotActiveTodayException {
        Habit habit = allHabits.get(habitId);
        if (habit == null) throw new HabitNotFoundException();

        DayOfWeek today = LocalDate.now().getDayOfWeek();
        if (!habit.getSchedule().contains(today)) throw new HabitNotActiveTodayException();

        habit.markCompletedToday();
        recomputeDashboardMetrics();
        Persistence.saveSnapshot();
    }

    // Unmark today's completion
    public static void unmarkHabitCompletedToday(UUID habitId) throws HabitNotFoundException {
        Habit habit = allHabits.get(habitId);
        if (habit == null) throw new HabitNotFoundException();

        habit.unmarkCompletedToday();
        recomputeDashboardMetrics();
        Persistence.saveSnapshot();
    }

    public static void setCompletedToday(UUID habitId, boolean completed)
            throws HabitNotFoundException, HabitAlreadyCompleteException, HabitNotActiveTodayException {
        if (completed) markHabitCompletedToday(habitId);
        else unmarkHabitCompletedToday(habitId);
        recomputeDashboardMetrics();
    }
}

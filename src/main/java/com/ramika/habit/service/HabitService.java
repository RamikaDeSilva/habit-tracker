package main.java.com.ramika.habit.service;

import main.java.com.ramika.habit.exceptions.AlreadyNotActiveException;
import main.java.com.ramika.habit.exceptions.HabitAlreadyCompleteException;
import main.java.com.ramika.habit.exceptions.HabitNotFoundException;
import main.java.com.ramika.habit.model.Category;
import main.java.com.ramika.habit.model.Habit;
import main.java.com.ramika.habit.model.Priority;
import main.java.com.ramika.habit.model.Status;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

// Singleton class to handle all habit functionality
public class HabitService {
    private static HabitService instance;
    private static Map<UUID, Habit> allHabits = new LinkedHashMap<>();
    private static Map<UUID, Habit> activeHabits = new LinkedHashMap<>();

    private HabitService() {}

    public static HabitService getInstance() {
        if (instance != null) {
            return instance;
        }
        instance = new HabitService();
        return instance;
    }

    public static Map<UUID, Habit> getAllHabits() {
        return allHabits;
    }

    public static Map<UUID, Habit> getActiveHabits() {
        return activeHabits;
    }

    // MODIFIES: allHabits, activeHabits
    // EFFECTS: Creates new habit with given name, priority, category
    // add the habit to allHabits and activeHabits with given id
    public static void createHabit(String name, Priority priority, Category category, EnumSet<DayOfWeek> schedule) {
        // create new uuid
        // call constructor
        // add to both lists
        UUID id = UUID.randomUUID();
        Habit habit = new Habit(id, name, priority, category, schedule);
        allHabits.put(id, habit);
        activeHabits.put(id, habit);
        // future : check for duplicate before adding
    }

    // MODIFIES: allHabits, activeHabits
    // EFFECTS: Removes habit from allHabits and activeHabits if active
    // if not in allHabits, throw exception - CREATE ONE FOR THIS!!!
    public static void removeHabit(UUID habitID) throws HabitNotFoundException {
        // check if UUID Is in Keyset, if not throw exception
        if (!allHabits.containsKey(habitID)) {
            throw new HabitNotFoundException();
        }
        else {
            Habit removedHabit = allHabits.get(habitID);
            if (removedHabit.getActiveStatus() == Status.ACTIVE) {
                if (!activeHabits.containsKey(habitID)) {
                    throw new HabitNotFoundException();
                } else {
                    activeHabits.remove(habitID);
                }
                allHabits.remove(habitID);
            } else {
                allHabits.remove(habitID);
            }
        }
    }

    // MODIFIES: allHabits, activeHabits
    // EFFECTS: deactivates habit - removes from activeHabits
    // if not in allHabits, throw HabitNotFoundException
    // if already notActive, throw AlreadyNotActiveException
    public static void deactivateHabit(UUID habitID) throws HabitNotFoundException, AlreadyNotActiveException {
        // check if UUID Is in Keyset, if not throw exception
        if (!allHabits.containsKey(habitID)) {
            throw new HabitNotFoundException();
        }
        else {
            Habit deactivatedHabit = allHabits.get(habitID);
            if (deactivatedHabit.getActiveStatus() == Status.ACTIVE) {
                deactivatedHabit.setActiveStatus(Status.INACTIVE);
                activeHabits.remove(habitID);
            } else {
               throw new AlreadyNotActiveException();
            }
        }
    }

    // EFFECTS: finds UUID with given habitName, if found, set found ID to
    // search id, otherwise do nothing
    public static UUID validIdExist(UUID searchId, String habitName) {
        //Map<UUID, Habit> habits = HabitService.getAllHabits();
        for (UUID id : allHabits.keySet()) {
            if (allHabits.get(id) != null) {
                String name = allHabits.get(id).getName().toLowerCase();
                if (name.equals(habitName)) {
                    searchId = id;
                }
            }
        }
        return searchId;
    }

    public static void changeName(UUID searchId, String name) {
        for (UUID id : allHabits.keySet()) {
            if (searchId.equals(id)) {
                if (allHabits.get(id) != null) {
                    allHabits.get(id).setName(name);
                    break;
                }
            }
        }
    }

    public static void changePriority(UUID searchId, Priority priority) {
        for (UUID id : allHabits.keySet()) {
            if (searchId.equals(id)) {
                if (allHabits.get(id) != null) {
                    allHabits.get(id).setPriority(priority);
                    break;
                }
            }
        }
    }

    public static void changeCategory(UUID searchId, Category category) {
        for (UUID id : allHabits.keySet()) {
            if (searchId.equals(id)) {
                if (allHabits.get(id) != null) {
                    allHabits.get(id).setCategory(category);
                    break;
                }
            }
        }
    }

    public static void markHabitCompletedToday(UUID habitId) throws HabitNotFoundException, HabitAlreadyCompleteException {
        Habit habit = allHabits.get(habitId);
        if (habit == null) {
            throw new HabitNotFoundException();
        }
        habit.markCompletedToday();
    }
}

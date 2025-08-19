package main.java.com.ramika.habit.service;

import main.java.com.ramika.habit.model.Category;
import main.java.com.ramika.habit.model.Habit;
import main.java.com.ramika.habit.model.Priority;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

// Singleton class to handle all habit functionality
public class HabitService {
    private HabitService instance;
    private static Map<UUID, Habit> allHabits;
    private static Map<UUID, Habit> activeHabits;

    private HabitService() {
        allHabits = new LinkedHashMap<>();
        activeHabits = new LinkedHashMap<>();
    }

    public HabitService getInstance() {
        if (instance != null) {
            return instance;
        }
        instance = new HabitService();
        return instance;
    }

    // MODIFIES: allHabits, activeHabits
    // EFFECTS: Creates new habit with given name, priority, category
    // add the habit to allHabits and activeHabits with given id
    public void createHabit(String name, Priority priority, Category category) {
        // create new uuid
        // call constructor
        // add to both lists
        UUID id = UUID.randomUUID();
        Habit habit = new Habit(id, name, priority, category);
        allHabits.put(id, habit);
        activeHabits.put(id, habit);
        //
    }

    // MODIFIES: allHabits, activeHabits
    // EFFECTS: Removes habit from allHabits and activeHabits if active
    // if not in allHabits, throw exception - CREATE ONE FOR THIS!!!
    public void deleteHabit() {
    }
}

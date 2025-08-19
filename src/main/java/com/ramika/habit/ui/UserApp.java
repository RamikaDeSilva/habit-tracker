package main.java.com.ramika.habit.ui;

import main.java.com.ramika.habit.exceptions.AlreadyNotActiveException;
import main.java.com.ramika.habit.exceptions.HabitNotFoundException;
import main.java.com.ramika.habit.model.Category;
import main.java.com.ramika.habit.model.Habit;
import main.java.com.ramika.habit.model.Priority;
import main.java.com.ramika.habit.service.HabitService;

import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class UserApp {
    private Scanner input;
    public UserApp() {
        runApp();
    }

    // source attribute to TellerApp:
    // https://github.students.cs.ubc.ca/CPSC210/TellerApp
    // MODIFIES: this
    // EFFECTS: processes user input
    private void runApp() {
        boolean keepGoing = true;
        input = new Scanner(System.in);
        String command;

        System.out.println("\nWelcome to your Personal Habit Tracker!");

        while (keepGoing) {
            displayMenu();
            command = input.next();
            command = command.toLowerCase();

            if (command.equals("q")) {
                keepGoing = false;
            } else {
                input.nextLine();
                processCommand(command);
            }
        }
        System.out.println("\nGoodbye!");
    }

    // source attribute to TellerApp:
    // https://github.students.cs.ubc.ca/CPSC210/TellerApp
    // MODIFIES: this
    // EFFECTS: processes user command
    private void processCommand(String command) {
        if (command.equals("a")) {
            System.out.println("\nPlease enter the following information: ");
            addHabit();
            System.out.println("\nYour habit has been added! Anything else?");
        } else if (command.equals("v")) {
            if (HabitService.getAllHabits().isEmpty()) {
                System.out.println("\nYou have no habits - please add a habit first.");
            } else {
                viewHabits(HabitService.getAllHabits());
            }
        } else if (command.equals("h")) {
            if (HabitService.getActiveHabits().isEmpty()) {
                System.out.println("\nYou have no habits - please add a habit first.");
            } else {
                viewHabits(HabitService.getActiveHabits());
            }
        } else if (command.equals("s")) {
            viewSpecificHabit();
        } else if (command.equals("i")) {
            deactivateHabit();
            System.out.println("Habit now deactivated - Anything else? ");
        } else if (command.equals("d")) {
            removeHabit();
            System.out.println("Habit deleted - Anything else? ");
        }
        else {
            System.out.println("Selection not valid...");
        }
    }


    // EFFECTS: displays menu of options to user
    private void displayMenu() {
        System.out.println("\nSelect from:");
        System.out.println("\ta -> Add a new habit");
        System.out.println("\tv -> View all habits");
        System.out.println("\th -> View active habits");
        System.out.println("\ts -> View a specific habit");
        System.out.println("\ti -> Make habit inactive");
        System.out.println("\td -> Delete habit");
        System.out.println("\tq -> quit");
    }

    // MODIFIES: HabitService
    // EFFECTS: creates and adds habit to active habit list and general habit list
    private void addHabit() {
        String title;
        Priority priority = null;
        Category category = null;
        do {
            System.out.print("enter title: ");
            title = input.nextLine().trim();
            if (title.isEmpty()) {
                System.out.println("error: please don't enter blank title for habit.");
            }
        } while (title.isEmpty());

        do {
            System.out.print("\nChose Priority by Letter: \n");
            System.out.println("a - High priority");
            System.out.println("b - Medium priority");
            System.out.println("c - Low priority");
            String stringPriority = input.next();
            stringPriority = stringPriority.toLowerCase();

            if (stringPriority.length() != 1) {
                System.out.println("error: please enter specific letter");
            }
            switch (stringPriority) {
                case "a":
                    priority = Priority.HIGH;
                    break;
                case "b":
                    priority = Priority.MEDIUM;
                    break;
                case "c":
                    priority = Priority.LOW;
                    break;
                default:
                    break;
            }
        } while (priority == null);

        do {
            System.out.print("\nChose Category by Letter: \n");
            System.out.println("a - Fitness");
            System.out.println("b - Financial");
            System.out.println("c - Mental Health");
            System.out.println("d - Other");
            String stringCategory = input.next();
            stringCategory = stringCategory.toLowerCase();

            if (stringCategory.length() != 1) {
                System.out.println("error: please enter specific letter");
            }
            switch (stringCategory) {
                case "a":
                    category = Category.FITNESS;
                    break;
                case "b":
                    category = Category.FINANCIAL;
                    break;
                case "c":
                    category = Category.MENTALHEALTH;
                    break;
                case "d":
                    category = Category.OTHER;
                    break;
                default:
                    break;
            }
        } while (category == null);

        HabitService.createHabit(title, priority, category);
    }

    // EFFECTS: Displays all habits in collection of habits given
    private void viewHabits(Map<UUID, Habit> habits) {
        for (UUID id : habits.keySet()) {
            try {
                System.out.println();
                viewHabit(id);
            } catch (HabitNotFoundException e) {
                System.out.println("error: habit doesn't exist");
            }
        }
    }

    // EFFECTS: View specific habit entered as input
    private void viewSpecificHabit() {
        UUID searchID = null;
        do {
            System.out.println("Enter habit name");
            String habitName = input.next().toLowerCase();
            Map<UUID, Habit> habits = HabitService.getAllHabits();
            for (UUID id : habits.keySet()) {
                if (habits.get(id) != null) {
                    String name = habits.get(id).getName().toLowerCase();
                    if (name.equals(habitName)) {
                        searchID = id;
                    }
                }
            }
        } while (searchID == null);

        try {
            viewHabit(searchID);
        } catch (HabitNotFoundException e) {
            System.out.println("error: habit doesn't exist");
        }
    }

    // EFFECTS: remove given habit
    private void removeHabit() {
        UUID searchID = null;
        do {
            System.out.println("Enter habit name");
            String habitName = input.next().toLowerCase();
            Map<UUID, Habit> habits = HabitService.getAllHabits();
            for (UUID id : habits.keySet()) {
                if (habits.get(id) != null) {
                    String name = habits.get(id).getName().toLowerCase();
                    if (name.equals(habitName)) {
                        searchID = id;
                    }
                }
            }
        } while (searchID == null);

        try {
            HabitService.removeHabit(searchID);
        } catch (HabitNotFoundException e) {
            System.out.println("error: habit doesn't exist");
        }
    }

    private void deactivateHabit() {
        UUID searchID = null;
        do {
            System.out.println("Enter habit name");
            String habitName = input.next().toLowerCase();
            Map<UUID, Habit> habits = HabitService.getAllHabits();
            for (UUID id : habits.keySet()) {
                if (habits.get(id) != null) {
                    String name = habits.get(id).getName().toLowerCase();
                    if (name.equals(habitName)) {
                        searchID = id;
                    }
                }
            }
        } while (searchID == null);

        try {
            HabitService.deactivateHabit(searchID);
        } catch (HabitNotFoundException e) {
            System.out.println("error: habit doesn't exist");
        } catch (AlreadyNotActiveException e) {
            System.out.println("error: habit already not active");
        }
    }

    // EFFECTS: displays specific habit asked
    private void viewHabit(UUID habitId) throws HabitNotFoundException {
        // get the habit from the general collection - go through ids and check if they match
        // if habit is not inside, throw new exception book not found
        if (!HabitService.getAllHabits().containsKey(habitId)) {
            throw new HabitNotFoundException();
        } else {
            Habit habit = null;
            for (UUID id : HabitService.getAllHabits().keySet()) {
                if (id.equals(habitId)) {
                    habit = HabitService.getAllHabits().get(id);
                }
            }
            if (habit != null) {
                System.out.println("Habit: " + habit.getName());
                System.out.println("Category: " + habit.getCategory());
                System.out.println("Priority level: " + habit.getPriority());
                System.out.println("Active? : " + habit.getActiveStatus());
            }
        }

    }

}

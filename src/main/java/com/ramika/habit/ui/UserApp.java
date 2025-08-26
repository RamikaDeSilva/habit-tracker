package main.java.com.ramika.habit.ui;

import main.java.com.ramika.habit.exceptions.AlreadyNotActiveException;
import main.java.com.ramika.habit.exceptions.HabitAlreadyCompleteException;
import main.java.com.ramika.habit.exceptions.HabitNotActiveTodayException;
import main.java.com.ramika.habit.exceptions.HabitNotFoundException;
import main.java.com.ramika.habit.model.Category;
import main.java.com.ramika.habit.model.Habit;
import main.java.com.ramika.habit.model.Priority;
import main.java.com.ramika.habit.service.HabitService;

import java.time.DayOfWeek;
import java.util.EnumSet;
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
        switch (command) {
            case "a" -> {
                System.out.println("\nPlease enter the following information: ");
                addHabit();
                System.out.println("\nYour habit has been added! Anything else?");
            }
            case "v" -> {
                if (HabitService.getAllHabits().isEmpty()) {
                    System.out.println("\nYou have no habits - please add a habit first.");
                } else {
                    viewHabits(HabitService.getAllHabits());
                }
            }
            case "h" -> {
                if (HabitService.getActiveHabits().isEmpty()) {
                    System.out.println("\nYou have no active habits - please add a habit first.");
                } else {
                    viewHabits(HabitService.getActiveHabits());
                }
            }
            case "s" -> viewSpecificHabit();
            case "e" -> {
                    editSpecificHabit();
                    System.out.println("edit has been made - Anything else? ");
            }
            case "i" -> {
                deactivateHabit();
                System.out.println("Habit now deactivated - Anything else? ");
            }
            case "c" -> {
                markHabitCompletedToday();
            }
            case "o" -> {
                viewHabitsCompletedToday();
            }
            case "d" -> {
                removeHabit();
                System.out.println("Habit deleted - Anything else? ");
            }
            default -> System.out.println("Selection not valid...");
        }
    }


    // EFFECTS: displays menu of options to user
    private void displayMenu() {
        System.out.println("\nSelect from:");
        System.out.println("\ta -> Add a new habit");
        System.out.println("\tv -> View all habits");
        System.out.println("\th -> View active habits");
        System.out.println("\ts -> View a specific habit");
        System.out.println("\te -> edit a habit");
        System.out.println("\ti -> Make habit inactive");
        System.out.println("\tc -> Mark habit complete today");
        System.out.println("\to -> View habits completed today");
        System.out.println("\td -> Delete habit");
        System.out.println("\tq -> quit");
    }

    // MODIFIES: HabitService
    // EFFECTS: creates and adds habit to active habit list and general habit list
    private void addHabit() {
        String title;
//        Priority priority = null;
//        Category category = null;
        do {
            System.out.print("enter title: ");
            title = input.nextLine().trim();
            if (title.isEmpty()) {
                System.out.println("error: please don't enter blank title for habit.");
            }
        } while (title.isEmpty());

        System.out.println();
        Priority priority = enterNewPriority();
        System.out.println();
        Category category = enterNewCategory();
        EnumSet<DayOfWeek> schedule = enterDaysOfWeek();
        System.out.println("Chosen Schedule: " + schedule);

        HabitService.createHabit(title, priority, category, schedule);
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
            searchID = HabitService.validIdExist(searchID, habitName);
        } while (searchID == null);

        try {
            viewHabit(searchID);
        } catch (HabitNotFoundException e) {
            System.out.println("error: habit doesn't exist");
        }
    }

    // MODIFIES: Habit instance found
    // EFFECTS: finds given habit, changes chosen thing to change
    private void editSpecificHabit() {
        UUID searchID = null;
        do {
            System.out.println("Enter habit name");
            String habitName = input.next().toLowerCase();
            searchID = HabitService.validIdExist(searchID, habitName);
        } while (searchID == null);

        boolean foundProperty = false;
        do {
            System.out.print("\nWhat do you want to edit: \n");
            System.out.println("a - name");
            System.out.println("b - priority level");
            System.out.println("c - category");
            String stringProperty = input.next();
            stringProperty = stringProperty.toLowerCase();

            if (stringProperty.length() != 1) {
                System.out.println("error: please enter specific letter");
            }
            switch (stringProperty) {
                case "a":
                    System.out.println("Enter the new name: ");
                    String newName = input.next().trim();
                    HabitService.changeName(searchID, newName);
                    foundProperty = true;
                    break;
                case "b":
                    Priority priorityChosen = enterNewPriority();
                    HabitService.changePriority(searchID, priorityChosen);
                    foundProperty = true;
                    break;
                case "c":
                    Category categoryChosen = enterNewCategory();
                    HabitService.changeCategory(searchID, categoryChosen);
                    foundProperty = true;
                    break;
                default:
                    break;
            }
        } while (!foundProperty);
    }

    private Priority enterNewPriority() {
        System.out.println("Enter the priority: ");
        Priority priority = null;
        do {
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
        return priority;
    }

    private Category enterNewCategory() {
        System.out.println("Enter the category: ");
        Category category = null;

        do {
            System.out.print("Chose Category by Letter: \n");
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
        return category;
    }

    private EnumSet<DayOfWeek> enterDaysOfWeek() {
        System.out.println("Enter the days you want this habit: ");
        EnumSet<DayOfWeek> schedule = EnumSet.noneOf(DayOfWeek.class);

        if (input.hasNextLine()) input.nextLine();

        do {
            for (DayOfWeek day : DayOfWeek.values()) {
                System.out.print("\nInclude " + day + "? (y/n): ");
                String answer = input.nextLine().trim().toLowerCase();
                if (answer.equals("y") || answer.equals("yes")) {
                    schedule.add(day);
                }
            }
            if (schedule.isEmpty()) {
                System.out.println("please choose at least one day you want this habit to occur.");
            }
        } while (schedule.isEmpty());
        return schedule;
    }

    // EFFECTS: remove given habit
    private void removeHabit() {
        UUID searchID = null;
        do {
            System.out.println("Enter habit name");
            String habitName = input.next().toLowerCase();
            searchID = HabitService.validIdExist(searchID, habitName);
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
            System.out.println("Enter habit name: ");
            String habitName = input.next().toLowerCase();
            searchID = HabitService.validIdExist(searchID, habitName);
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
                System.out.println("Schedule: " + habit.getSchedule());
                System.out.println("Completion History: " + habit.getCompletions());
            }
        }

    }

    private void markHabitCompletedToday() {
        UUID searchID = null;
        do {
            System.out.println("Enter habit name: ");
            String habitName = input.next().toLowerCase();
            searchID = HabitService.validIdExist(searchID, habitName);
        } while (searchID == null);

        try {
            HabitService.markHabitCompletedToday(searchID);
            System.out.println("Habit marked complete for today - Anything else? ");
        } catch (HabitNotFoundException e) {
            System.out.println("error: habit doesn't exist");
        } catch (HabitAlreadyCompleteException e) {
            System.out.println("error: habit already completed today");
        } catch (HabitNotActiveTodayException e) {
            System.out.println("error: habit not active today");
        }
    }

    // EFFECTS: displays active habits completed today
    private void viewHabitsCompletedToday() {
        boolean found = false;
        for (Habit habit : HabitService.getActiveHabits().values()) {
            if (habit.findCompletionByDate(java.time.LocalDate.now()) != null) {
                System.out.println("Habit: " + habit.getName());
                found = true;
            }
        }
        if (!found) {
            System.out.println("No habits completed today.");
        }
    }
}

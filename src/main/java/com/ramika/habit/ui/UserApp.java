package main.java.com.ramika.habit.ui;

import main.java.com.ramika.habit.model.Habit;
import main.java.com.ramika.habit.service.HabitService;

import java.util.Map;
import java.util.Scanner;
import java.util.Set;
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
        String command = null;

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
        System.out.println("hi");
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
            viewHabit();
        } else {
            System.out.println("Selection not valid...");
        }
    }


    // source attribute to TellerApp:
    // https://github.students.cs.ubc.ca/CPSC210/TellerApp
    // EFFECTS: displays menu of options to user
    private void displayMenu() {
        System.out.println("\nSelect from:");
        System.out.println("\ta -> Add a new habit");
        System.out.println("\tv -> View all habits");
        System.out.println("\th -> View active habits");
        System.out.println("\ts -> View a specific habit");
        System.out.println("\tq -> quit");
    }

    // MODIFIES: HabitService
    // EFFECTS: creates and adds habit to active habit list and general habit list
    private void addHabit() {
        return;
    }

    // EFFECTS: Displays all habits in collection of habits given
    private void viewHabits(Map<UUID, Habit> habits) {
        return;
    }

    // displays specific habit asked
    private void viewHabit() {
        return;
    }

}

package users.ui;

import java.util.Scanner;

/**
 * Base class for user interface (UI) classes.
 * Provides common functionality for displaying menus, clearing the screen, and handling input.
 */
public abstract class BaseUI {

    protected final Scanner scanner;

    /**
     * Constructor for BaseUI.
     * Initializes the shared scanner instance.
     */
    public BaseUI() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Displays the menu header with a given title.
     *
     * @param title the title of the menu
     */
    protected void displayMenuHeader(String title) {
        // Optionally clear the console; comment/remove if causing flickering
        System.out.println("\n");
        System.out.println("===============================================");
        System.out.printf("| %-45s |\n", title);
        System.out.println("===============================================");
    }


    /**
     * Displays a menu option.
     *
     * @param optionNumber the number of the menu option
     * @param description  the description of the menu option
     */
    protected void displayMenuOption(int optionNumber, String description) {
        System.out.printf("| %2d. %-42s |\n", optionNumber, description);
    }

    /**
     * Displays a menu footer and prompts the user for input.
     *
     * @return the user's menu choice as an integer
     */
    protected int getMenuChoice() {
        System.out.println("===============================================");
        System.out.print("Enter your choice: ");
        try {
            return scanner.nextInt();
        } catch (Exception e) {
            scanner.nextLine(); // Clear invalid input
            System.out.println("Invalid Input!");
            return -1; // Return an invalid choice indicator
        }
    }

    /**
     * Pauses execution until the user presses Enter.
     */
    protected void pauseForUser() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Validates the user's menu choice against the available options.
     *
     * @param choice the user's menu choice
     * @param min    the minimum valid menu option
     * @param max    the maximum valid menu option
     * @return true if the choice is valid, false otherwise
     */
    protected boolean isValidChoice(int choice, int min, int max) {
        return choice >= min && choice <= max;
    }

    /**
     * Displays a generic error message for invalid input.
     */
    protected void displayInvalidInputMessage() {
        System.out.println("\nInvalid input. Please try again.");
    }

    /**
     * Abstract method for displaying the menu.
     * Must be implemented by subclasses to define their specific menus.
     */
    public abstract void displayMenu();
}

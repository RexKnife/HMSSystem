package utils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.util.function.Predicate;

/**
 * Utility class for prompting user input with validation.
 */
public class PromptUtils {

    /**
     * Prompts the user for a non-empty string.
     *
     * @param scanner   the scanner for user input
     * @param promptMsg the message to display
     * @param fieldName the name of the field for validation messages
     * @return the entered string, or null if the user exits
     */
    public static String promptNonEmptyString(Scanner scanner, String promptMsg, String fieldName) {
        while (true) {
            System.out.print(promptMsg);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) return null;
            try {
                return ValidationUtils.validateString(input, fieldName);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Prompts the user for a valid date.
     *
     * @param scanner   the scanner for user input
     * @param promptMsg the message to display
     * @param fieldName the name of the field for validation messages
     * @param validator the validator for additional date constraints
     * @return the entered date, or null if the user exits
     */
    public static LocalDate promptDate(Scanner scanner, String promptMsg, String fieldName, Predicate<LocalDate> validator) {
        while (true) {
            System.out.print(promptMsg);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) return null;
            try {
                LocalDate date = LocalDate.parse(input);
                if (validator != null && !validator.test(date)) {
                    System.out.println(fieldName + " does not meet the specified requirements.");
                    continue;
                }
                return date;
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            }
        }
    }

    /**
     * Prompts the user for gender selection.
     *
     * @param scanner   the scanner for user input
     * @param promptMsg the message to display
     * @return the selected gender, or null if the user exits
     */
    public static String promptGender(Scanner scanner, String promptMsg) {
        String[] options = {"Male", "Female"};
        return promptForSelection(scanner, options, promptMsg);
    }

    /**
     * Prompts the user for a valid blood type.
     *
     * @param scanner   the scanner for user input
     * @param promptMsg the message to display
     * @return the selected blood type, or null if the user exits
     */
    public static String promptBloodType(Scanner scanner, String promptMsg) {
        String[] bloodTypes = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        return promptForSelection(scanner, bloodTypes, promptMsg);
    }

    /**
     * Prompts the user for a valid email address.
     *
     * @param scanner   the scanner for user input
     * @param promptMsg the message to display
     * @return the entered email, or null if the user exits
     */
    public static String promptEmail(Scanner scanner, String promptMsg) {
        while (true) {
            System.out.print(promptMsg);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) return null;
            if (ValidationUtils.isValidEmail(input)) {
                return input;
            }
            System.out.println("Invalid email format. Please try again.");
        }
    }

    /**
     * Prompts the user for a unique ID, ensuring it is valid and not a duplicate.
     *
     * @param scanner    the scanner for user input
     * @param promptMsg  the message to display
     * @param fieldName  the name of the field for validation messages
     * @param uniqueness a predicate to ensure uniqueness of the ID
     * @return the entered unique ID, or null if the user exits
     */
    public static String promptUniqueID(Scanner scanner, String promptMsg, String fieldName, Predicate<String> uniqueness) {
        while (true) {
            System.out.print(promptMsg);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) return null;
            try {
                String validatedInput = ValidationUtils.validateString(input, fieldName);
                if (!uniqueness.test(validatedInput)) {
                    System.out.println("The " + fieldName + " must be unique and is already in use.");
                    continue;
                }
                return validatedInput;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Prompts the user to select an option from a list.
     *
     * @param scanner   the scanner for user input
     * @param options   the list of options
     * @param promptMsg the message to display
     * @return the selected option, or null if the user exits
     */
    public static String promptForSelection(Scanner scanner, String[] options, String promptMsg) {
        while (true) {
            System.out.println(promptMsg);
            for (int i = 0; i < options.length; i++) {
                System.out.printf("%d. %s%n", i + 1, options[i]);
            }
            System.out.print("Choose a number (or type 'exit' to cancel): ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) return null;
            try {
                int choice = Integer.parseInt(input);
                if (choice > 0 && choice <= options.length) {
                    return options[choice - 1];
                }
            } catch (NumberFormatException ignored) {
                System.out.println("Invalid choice. Please select a valid number.");
            }
        }
    }
}

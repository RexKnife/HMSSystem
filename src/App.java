import users.Users;
import users.authorization.AuthorizationControl;
import users.usermgmt.PasswordManagement;

import datamgmt.retrievers.*;

import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        authoriseAndDisplayMenu();
    }

    public static void authoriseAndDisplayMenu() {
        // Initialize necessary components
        AuthorizationControl authControl = new AuthorizationControl();
        StaffData staffData = new StaffData();
        PatientData patientData = new PatientData();
        MedicineData medicineData = new MedicineData();
        AppointmentData appointmentData = new AppointmentData();
        ReplenishmentRequestData replenishmentRequestData = new ReplenishmentRequestData();

        // Import initial data
        staffData.importData();
        patientData.importData();
        medicineData.importData();
        appointmentData.importData();
        replenishmentRequestData.importData();

        // Initialize PasswordManagement
        PasswordManagement passwordManagement = new PasswordManagement(staffData, patientData);

        // Load credentials from staff and patient data
        authControl.loadCredentials(staffData, patientData);

        Scanner scanner = new Scanner(System.in);
        boolean loggedIn = false;

        System.out.println("===================================");
        System.out.println(" Welcome to the Health Management System!");
        System.out.println("===================================");

        while (!loggedIn) {
            System.out.println("\nPlease choose an option:");
            System.out.println(" 1. Login");
            System.out.println(" 2. Exit");
            System.out.println("===================================");

            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine().trim();

            if ("2".equals(choice)) {
                System.out.println("Exiting the application...");
                break;
            } else if (!"1".equals(choice)) {
                System.out.println("Invalid choice. Please enter 1 to login or 2 to exit.");
                continue;
            }

            System.out.println("==================================="); 
            System.out.println("\nPlease log in:");
            System.out.println("===================================");

            try {
                System.out.print("User ID: ");
                String userID = scanner.nextLine().trim();

                System.out.print("Password: ");
                String password = scanner.nextLine().trim();

                if (userID.isEmpty() || password.isEmpty()) {
                    System.out.println("User ID and Password cannot be empty. Please try again.");
                    continue;
                }

                // Attempt to log in
                Users user = authControl.login(userID, password);

                if (user != null) {
                    // Check for default password
                    if ("defaultPassword".equals(password)) {
                        System.out.println("\nYou are using the default password. Please update it for security purposes.");
                        passwordManagement.changePassword(scanner);
                    }

                    // Check authorization and display menu
                    if (authControl.authorize(userID, user.getRole().name())) {
                        System.out.println("\n===================================");
                        System.out.println("Welcome, " + user.getName() + ".");
                        System.out.println("===================================\n");

                        // Ensure password was changed if it was the default
                        if (!"defaultPasswords".equals(password) || 
                            (!"defaultPasswords".equals(user.getPassword()))) {
                            user.getOrCreateUI(); // Display the user-specific menu
                            loggedIn = true;
                        }
                    } else {
                        System.out.println("Authorization failed. Access denied.");
                    }
                } else {
                    System.out.println("Login failed. Please check your credentials and try again.");
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }

        scanner.close();
        System.out.println("Thank you for using the application. Goodbye!");
    }
}

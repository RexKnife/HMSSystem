package users.staff.administrator;

import datamgmt.retrievers.StaffData;
import users.ui.BaseUI;
import utils.enums.Gender;
import utils.enums.Roles;
import users.staff.StaffCRUD;

import java.util.Scanner;

/**
 * Administrator UI for managing hospital staff.
 * Provides functionality to view, add, search, update, and delete staff members.
 */
public class AdministratorStaffManagementUI extends BaseUI {

    private final StaffCRUD staffCRUD;
    private final Scanner scanner;

    /**
     * Constructs the AdministratorStaffManagementUI with a `StaffCRUD` instance.
     *
     * @param scanner the Scanner for user input
     */
    public AdministratorStaffManagementUI(Scanner scanner) {
        StaffData staffData = new StaffData();
        staffData.reloadData();
        this.staffCRUD = new StaffCRUD(staffData);
        this.scanner = scanner;
    }

    @Override
    public void displayMenu() {
        while (true) {
            displayMenuHeader("STAFF MANAGEMENT MENU");
            displayMenuOption(1, "View All Staff");
            displayMenuOption(2, "Add New Staff");
            displayMenuOption(3, "Search for Staff");
            displayMenuOption(4, "Update Staff Details");
            displayMenuOption(5, "Delete Staff");
            displayMenuOption(6, "Go Back to Main Menu");

            int choice = getMenuChoice();

            if (!isValidChoice(choice, 1, 6)) {
                displayInvalidInputMessage();
                pauseForUser(scanner);
                continue;
            }

            try {
                switch (choice) {
                    case 1:
                        viewAllStaff();
                        break;
                    case 2:
                        addNewStaff();
                        break;
                    case 3:
                        searchStaff();
                        break;
                    case 4:
                        updateStaffDetails();
                        break;
                    case 5:
                        deleteStaff();
                        break;
                    case 6:
                        System.out.println("Returning to the main menu...");
                        pauseForUser(scanner);
                        return;
                    default:
                        displayInvalidInputMessage();
                }
            } catch (Exception e) {
                System.err.println("An unexpected error occurred: " + e.getMessage());
            }

            pauseForUser(scanner);
        }
    }

    /**
     * Displays all staff members in the system.
     */
    private void viewAllStaff() {
        displayMenuHeader("ALL STAFF MEMBERS");
        staffCRUD.viewAllStaff();
    }

    
    /**
     * Adds a new staff member with enhanced error handling, validation, and exit option.
     */
    private void addNewStaff() {
        displayMenuHeader("ADD NEW STAFF");
        String defaultPassword = "defaultPassword";

        try {
            // Name Input
            String name = null;
            while (true) {
                System.out.print("Enter Name (or press Enter to cancel): ");
                name = scanner.nextLine().trim();
                if (name.isEmpty()) {
                    System.out.println("Operation canceled.");
                    return;
                }
                if (!name.matches("^[a-zA-Z ]+$")) {
                    System.out.println("Name must contain only letters and spaces. Please try again.");
                    continue;
                }
                break;
            }

            // Role Input
            Roles role = null;
            while (true) {
                System.out.print("Enter Role (DOCTOR/ADMINISTRATOR/PHARMACIST, or press Enter to cancel): ");
                String roleInput = scanner.nextLine().trim().toUpperCase();
                if (roleInput.isEmpty()) {
                    System.out.println("Operation canceled.");
                    return;
                }
                try {
                    role = Roles.valueOf(roleInput);
                    break;
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid role. Please enter DOCTOR, ADMINISTRATOR, or PHARMACIST.");
                }
            }

            // Gender Input
            Gender gender = null;
            while (true) {
                System.out.print("Enter Gender (MALE/FEMALE/OTHER, or press Enter to cancel): ");
                String genderInput = scanner.nextLine().trim().toUpperCase();
                if (genderInput.isEmpty()) {
                    System.out.println("Operation canceled.");
                    return;
                }
                try {
                    gender = Gender.valueOf(genderInput);
                    break;
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid gender. Please enter MALE or FEMALE");
                }
            }

            // Age Input
            Integer age = null;
            while (true) {
                System.out.print("Enter Age (or press Enter to cancel): ");
                String ageInput = scanner.nextLine().trim();
                if (ageInput.isEmpty()) {
                    System.out.println("Operation canceled.");
                    return;
                }
                try {
                    age = Integer.parseInt(ageInput);
                    if (age <= 0) {
                        System.out.println("Age must be a positive number. Please try again.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid age. Please enter a numeric value.");
                }
            }

            // Add Staff
            staffCRUD.addStaff(name, role, gender, age, defaultPassword);
            System.out.println("Staff member added successfully.");

        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }
    }



    /**
     * Searches for staff members based on filters.
     */
    private void searchStaff() {
        displayMenuHeader("SEARCH STAFF");

        try {
            System.out.print("Enter User ID (or press Enter to skip): ");
            String userID = scanner.nextLine().trim();
            if (userID.isEmpty()) userID = null;

            System.out.print("Enter Name (or press Enter to skip): ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) name = null;

            System.out.print("Enter Role (DOCTOR/ADMINISTRATOR/PHARMACIST or press Enter to skip): ");
            String roleInput = scanner.nextLine().trim();
            Roles role = roleInput.isEmpty() ? null : Roles.valueOf(roleInput.toUpperCase());

            staffCRUD.searchStaff(userID, name, role);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid input: " + e.getMessage());
        }
    }

    /**
     * Updates the details of a staff member.
     */
    private void updateStaffDetails() {
        displayMenuHeader("UPDATE STAFF DETAILS");

        try {
            System.out.print("Enter User ID of the Staff to Update: ");
            String userID = scanner.nextLine().trim();

            System.out.print("Enter New Name (or press Enter to skip): ");
            String newName = scanner.nextLine().trim();
            if (newName.isEmpty()) newName = null;

            System.out.print("Enter New Age (or press Enter to skip): ");
            String ageInput = scanner.nextLine().trim();
            Integer newAge = ageInput.isEmpty() ? null : Integer.parseInt(ageInput);

            System.out.print("Enter New Role (DOCTOR/ADMINISTRATOR/PHARMACIST or press Enter to skip): ");
            String roleInput = scanner.nextLine().trim();
            Roles newRole = roleInput.isEmpty() ? null : Roles.valueOf(roleInput.toUpperCase());

            System.out.print("Enter New Gender (MALE/FEMALE/OTHER or press Enter to skip): ");
            String genderInput = scanner.nextLine().trim();
            Gender newGender = genderInput.isEmpty() ? null : Gender.valueOf(genderInput.toUpperCase());

            System.out.print("Enter New Password (or press Enter to skip): ");
            String newPassword = scanner.nextLine().trim();
            if (newPassword.isEmpty()) newPassword = null;

            staffCRUD.updateStaff(userID, newName, newAge, newRole, newGender, newPassword);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid input: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An error occurred while updating staff: " + e.getMessage());
        }
    }

    /**
     * Deletes a staff member with error handling.
     */
    private void deleteStaff() {
        displayMenuHeader("DELETE STAFF");

        try {
            System.out.print("Enter User ID of the Staff to Delete: ");
            String userID = scanner.nextLine().trim();
            staffCRUD.deleteStaff(userID);
        } catch (Exception e) {
            System.err.println("An error occurred while deleting staff: " + e.getMessage());
        }
    }

    /**
     * Prompts the user to press Enter to continue.
     */
    private void pauseForUser(Scanner scanner) {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
}

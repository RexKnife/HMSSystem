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
    private Scanner scanner;
    /**
     * Constructs the AdministratorStaffManagementUI with a `StaffCRUD` instance.
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
                pauseForUser(this.scanner);
                continue;
            }

            switch (choice) {
                case 1:
                    viewAllStaff();
                    break;
                case 2:
                    addNewStaff(this.scanner);
                    break;
                case 3:
                    searchStaff(this.scanner);
                    break;
                case 4:
                    updateStaffDetails(this.scanner);
                    break;
                case 5:
                    deleteStaff(this.scanner);
                    break;
                case 6:
                    System.out.println("Returning to the main menu...");
                    pauseForUser(this.scanner);
                    return;
                default:
                    displayInvalidInputMessage();
                    break;
            }
            pauseForUser(this.scanner);
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
     * Adds a new staff member.
     */
    private void addNewStaff(Scanner scanner) {
        displayMenuHeader("ADD NEW STAFF");

        try {
            System.out.print("Enter Name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Enter Role (DOCTOR/ADMINISTRATOR/PHARMACIST): ");
            Roles role = Roles.valueOf(scanner.nextLine().trim().toUpperCase());

            System.out.print("Enter Gender (MALE/FEMALE/OTHER): ");
            Gender gender = Gender.valueOf(scanner.nextLine().trim().toUpperCase());

            System.out.print("Enter Age: ");
            int age = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Password: ");
            String password = scanner.nextLine().trim();

            staffCRUD.addStaff(name, role, gender, age, password);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid input: " + e.getMessage());
        }
    }

    /**
     * Searches for staff members based on filters.
     */
    private void searchStaff(Scanner scanner) {
        displayMenuHeader("SEARCH STAFF");

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
    }

    /**
     * Updates the details of a staff member.
     */
    private void updateStaffDetails(Scanner scanner) {
        displayMenuHeader("UPDATE STAFF DETAILS");

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
    }

    /**
     * Deletes a staff member.
     */
    private void deleteStaff(Scanner scanner) {
        displayMenuHeader("DELETE STAFF");

        System.out.print("Enter User ID of the Staff to Delete: ");
        String userID = scanner.nextLine().trim();

        staffCRUD.deleteStaff(userID);
    }

    /**
     * Prompts the user to press Enter to continue.
     */
    private void pauseForUser(Scanner scanner) {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
}
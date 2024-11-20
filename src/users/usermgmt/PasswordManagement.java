package users.usermgmt;

import users.*;
import users.authorization.AuthorizationControl;
import users.patient.Patient;
import utils.ValidationUtils;

import datamgmt.retrievers.*;

import java.io.IOException;
import java.util.Scanner;

/**
 * Handles password management for both staff and patients.
 * Supports changing and validating passwords.
 */
public class PasswordManagement {

    private final StaffData staffData;
    private final PatientData patientData;

    /**
     * Constructs the PasswordManagement instance with data access objects.
     *
     * @param staffData   the staff data handler
     * @param patientData the patient data handler
     */
    public PasswordManagement(StaffData staffData, PatientData patientData) {
        this.staffData = staffData;
        this.patientData = patientData;
    }

    /**
     * Handles password change functionality for the current user.
     *
     * @param scanner the scanner for user input
     */
    public void changePassword(Scanner scanner) {
        // Get the current user's ID from the authorization session
        String currentUserID = AuthorizationControl.getCurrentUserId();
        if (ValidationUtils.isNullOrEmpty(currentUserID)) {
            System.out.println("No active user session found.");
            return;
        }

        // Identify the user by ID
        Users user = staffData.findUserById(currentUserID);
        if (user == null) {
            user = patientData.findPatientById(currentUserID);
        }

        if (user == null) {
            System.out.println("User not found.");
            return;
        }

        System.out.println("Current User: " + user.getName() + " (" + user.getUserID() + ")");

        // Prompt for a new password
        String newPassword = promptForPassword(scanner, user);

        // Update and save the password
        if (user instanceof Patient) {
            patientData.updatePassword((Patient) user, PasswordHasher.hashPassword(newPassword));
        } else {
            try {
                staffData.updatePassword(user, PasswordHasher.hashPassword(newPassword));
            } catch (IOException e) {
                System.out.println("Password could not be changed, please try again.");
            }
        }

        System.out.println("Password changed successfully.");
    }

    /**
     * Prompts the user to enter a new password, validates it, and ensures it is different from the current password.
     *
     * @param scanner the scanner for user input
     * @param user    the user whose password is being changed
     * @return the validated new password
     */
    private String promptForPassword(Scanner scanner, Users user) {
        String newPassword;
        while (true) {
            System.out.print("Enter new password: ");
            newPassword = scanner.nextLine();

            try {
                // Validate the password using ValidationUtils
                ValidationUtils.validatePassword(newPassword);

                // Check if the new password is different from the current password
                if (user.getPassword().equals(PasswordHasher.hashPassword(newPassword))) {
                    throw new IllegalArgumentException("New password cannot be the same as the current password.");
                }
                break; // Exit loop if the password is valid
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid password: " + e.getMessage());
            }
        }
        return newPassword;
    }
}

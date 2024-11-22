package users.staff.administrator;

import users.patient.Patient;
import users.patient.PatientCRUD;
import datamgmt.retrievers.PatientData;
import users.ui.BaseUI;
import utils.enums.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Provides a user interface for administrators to manage patient data.
 */
public class AdministratorPatientManagementUI extends BaseUI {

    private final PatientCRUD patientCRUD;
    private final Scanner scanner;

    /**
     * Constructs the AdministratorPatientManagementUI with the given PatientCRUD handler.
     *
     * @param patientData the PatientData object for managing patients
     * @param scanner     the Scanner for user input
     */
    public AdministratorPatientManagementUI(PatientData patientData, Scanner scanner) {
        this.patientCRUD = new PatientCRUD(patientData);
        this.scanner = scanner;
    }

    /**
     * Displays the menu and handles user interactions.
     */
    @Override
    public void displayMenu() {
        while (true) {
            displayMenuHeader("Administrator Patient Management");
            displayMenuOption(1, "Search Patients");
            displayMenuOption(2, "Add New Patient");
            displayMenuOption(3, "Update Patient");
            displayMenuOption(4, "Delete Patient");
            displayMenuOption(5, "Exit");
            int choice = getMenuChoice();

            if (!isValidChoice(choice, 1, 5)) {
                displayInvalidInputMessage();
                continue;
            }

            try {
                switch (choice) {
                    case 1:
                        searchPatients();
                        break;
                    case 2:
                        addPatient();
                        break;
                    case 3:
                        updatePatient();
                        break;
                    case 4:
                        deletePatient();
                        break;
                    case 5:
                        System.out.println("Exiting...");
                        return;
                    default:
                        displayInvalidInputMessage();
                }
            } catch (Exception e) {
                System.err.println("An error occurred: " + e.getMessage());
            }

            pauseForUser();
        }
    }

    /**
     * Allows the administrator to search for patients by ID or name.
     */
    private void searchPatients() {
        try {
            System.out.print("Enter Patient ID (optional): ");
            String patientID = scanner.nextLine().trim();
            System.out.print("Enter Patient Name (optional): ");
            String name = scanner.nextLine().trim();

            List<Patient> results = patientCRUD.searchPatients(
                    patientID.isEmpty() ? null : patientID,
                    name.isEmpty() ? null : name
            );

            if (results.isEmpty()) {
                System.out.println("No matching patients found.");
            } else {
                System.out.printf("%-15s %-20s %-10s %-15s %-20s%n", "Patient ID", "Name", "Gender", "Age", "Contact Info");
                System.out.println("-------------------------------------------------------------------------------");
                for (Patient patient : results) {
                    System.out.printf("%-15s %-20s %-10s %-15d %-20s%n",
                            patient.getUserID(),
                            patient.getName(),
                            patient.getGender(),
                            patient.getAge(),
                            patient.getContactInfo());
                }
            }
        } catch (Exception e) {
            System.err.println("Error searching patients: " + e.getMessage());
        }
    }

    /**
     * Allows the administrator to add a new patient with email-based contact information.
     */
    private void addPatient() {
        displayMenuHeader("ADD NEW PATIENT");

        try {
            // Patient Name Input
            String name = null;
            while (true) {
                System.out.print("Enter Patient Name (or press Enter to cancel): ");
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

            // Patient Date of Birth Input
            LocalDate dob = null;
            while (true) {
                System.out.print("Enter Patient Date of Birth (YYYY-MM-DD, or press Enter to cancel): ");
                String dobInput = scanner.nextLine().trim();
                if (dobInput.isEmpty()) {
                    System.out.println("Operation canceled.");
                    return;
                }
                try {
                    dob = LocalDate.parse(dobInput);
                    break;
                } catch (Exception e) {
                    System.out.println("Invalid date format. Please use YYYY-MM-DD.");
                }
            }

            // Patient Gender Input
            Gender gender = null;
            while (true) {
                System.out.print("Enter Patient Gender (MALE/FEMALE/OTHER, or press Enter to cancel): ");
                String genderInput = scanner.nextLine().trim().toUpperCase();
                if (genderInput.isEmpty()) {
                    System.out.println("Operation canceled.");
                    return;
                }
                try {
                    gender = Gender.valueOf(genderInput);
                    break;
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid gender. Please enter MALE, FEMALE, or OTHER.");
                }
            }

            // Patient Blood Type Input
            String bloodType = null;
            while (true) {
                System.out.print("Enter Patient Blood Type (or press Enter to cancel): ");
                bloodType = scanner.nextLine().trim();
                if (bloodType.isEmpty()) {
                    System.out.println("Operation canceled.");
                    return;
                }
                if (!bloodType.matches("^(A|B|AB|O)[+-]$")) {
                    System.out.println("Invalid blood type. Please enter a valid blood type (e.g., A+, O-).");
                    continue;
                }
                break;
            }

            // Patient Email Input
            String email = null;
            while (true) {
                System.out.print("Enter Patient Email (or press Enter to cancel): ");
                email = scanner.nextLine().trim();
                if (email.isEmpty()) {
                    System.out.println("Operation canceled.");
                    return;
                }
                if (!email.matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                    System.out.println("Invalid email address. Please enter a valid email.");
                    continue;
                }
                break;
            }

            // Add Patient
            patientCRUD.createPatient(name, dob, gender, bloodType, email);
            System.out.println("Patient added successfully.");

        } catch (Exception e) {
            System.err.println("An unexpected error occurred while adding the patient: " + e.getMessage());
        }
    }



    /**
     * Allows the administrator to update a specific field of a patient.
     */
    private void updatePatient() {
        try {
            System.out.print("Enter Patient ID: ");
            String patientID = scanner.nextLine().trim();
            System.out.print("Enter Field to Update (name, dateOfBirth, contactInfo, bloodType): ");
            String field = scanner.nextLine().trim();
            System.out.print("Enter New Value: ");
            String value = scanner.nextLine().trim();

            if (field.equalsIgnoreCase("dateOfBirth")) {
                LocalDate newDate = parseDate(value);
                if (newDate == null) {
                    System.out.println("Invalid date format. Operation canceled.");
                    return;
                }
                patientCRUD.updatePatientField(patientID, field, newDate);
            } else {
                patientCRUD.updatePatientField(patientID, field, value);
            }
            System.out.println("Patient record updated successfully.");
        } catch (Exception e) {
            System.err.println("Error updating patient: " + e.getMessage());
        }
    }

    /**
     * Allows the administrator to delete a patient by ID.
     */
    private void deletePatient() {
        try {
            System.out.print("Enter Patient ID: ");
            String patientID = scanner.nextLine().trim();
            patientCRUD.deletePatient(patientID);
            System.out.println("Patient deleted successfully.");
        } catch (Exception e) {
            System.err.println("Error deleting patient: " + e.getMessage());
        }
    }

    /**
     * Parses a date from a string input.
     *
     * @param dateStr the date string
     * @return the parsed LocalDate, or null if invalid
     */
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            System.err.println("Invalid date format: " + e.getMessage());
            return null;
        }
    }
}

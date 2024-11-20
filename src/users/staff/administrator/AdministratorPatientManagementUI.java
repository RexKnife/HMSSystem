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
    private Scanner scanner;
    /**
     * Constructs the AdministratorPatientManagementUI with the given PatientCRUD handler.
     *
     * @param patientData the PatientData object for managing patients
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

            // Handle menu choices using a traditional switch statement
            switch (choice) {
                case 1:
                    searchPatients(this.scanner);
                    break;
                case 2:
                    addPatient(this.scanner);
                    break;
                case 3:
                    updatePatient(this.scanner);
                    break;
                case 4:
                    deletePatient(this.scanner);
                    break;
                case 5:
                    System.out.println("Exiting...");
                    return; // Exit the menu loop
                default:
                    displayInvalidInputMessage();
            }

            pauseForUser();
        }
    }

    /**
     * Allows the administrator to search for patients by ID or name.
     */
    private void searchPatients(Scanner scanner) {
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
    }

    /**
     * Allows the administrator to add a new patient.
     */
    private void addPatient(Scanner scanner) {
        System.out.print("Enter Patient Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter Patient Date of Birth (YYYY-MM-DD): ");
        LocalDate dob = parseDate(scanner.nextLine().trim());
        if (dob == null) {
            System.out.println("Invalid date format. Operation canceled.");
            return;
        }
        System.out.print("Enter Patient Gender (Male/Female): ");
        String genderInput = scanner.nextLine().trim();
        Gender gender = Gender.fromString(genderInput);
        if (gender == null) {
            System.out.println("Invalid gender input. Operation canceled.");
            return;
        }
        System.out.print("Enter Patient Blood Type: ");
        String bloodType = scanner.nextLine().trim();
        System.out.print("Enter Patient Contact Info: ");
        String contactInfo = scanner.nextLine().trim();

        patientCRUD.createPatient(name, dob, gender, bloodType, contactInfo);
    }

    /**
     * Allows the administrator to update a specific field of a patient.
     */
    private void updatePatient(Scanner scanner) {
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
    }

    /**
     * Allows the administrator to delete a patient by ID.
     */
    private void deletePatient(Scanner scanner) {
        System.out.print("Enter Patient ID: ");
        String patientID = scanner.nextLine().trim();
        patientCRUD.deletePatient(patientID);
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
            return null;
        }
    }
}

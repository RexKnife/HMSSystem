package users.staff.pharmacist;

import datamgmt.retrievers.AppointmentData;
import datamgmt.retrievers.MedicineData;
import datamgmt.retrievers.ReplenishmentRequestData;
import utils.enums.PrescriptionStatus;
import utils.medicinemanagements.MedicineManager;
import utils.medicinemanagements.ReplenishmentRequestManager;
import utils.appointments.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import utils.medicalrecords.*;
/**
 * Provides the Pharmacist menu to manage prescriptions, inventory, and replenishment requests.
 */
public class PharmacistUI {

    private final MedicineManager medicineManager;
    private final ReplenishmentRequestManager replenishmentRequestManager;
    private final AppointmentCRUD appointmentCRUD;
    private Pharmacist pharmacist;
    /**
     * Initializes the PharmacistUI with necessary managers.
     */
    public PharmacistUI(Pharmacist pharmacist) {
        MedicineData medicineData = new MedicineData();
        medicineData.importData();

        ReplenishmentRequestData requestData = new ReplenishmentRequestData();
        requestData.importData();

        AppointmentData appointmentData = new AppointmentData();
        appointmentData.importData();

        this.medicineManager = new MedicineManager(medicineData);
        this.replenishmentRequestManager = new ReplenishmentRequestManager(requestData);
        this.appointmentCRUD = new AppointmentCRUD(appointmentData);
        this.pharmacist = pharmacist;
    }

    /**
     * Displays the main menu for the pharmacist.
     */
    public void displayMenu() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n--- Pharmacist Menu ---");
            System.out.println("1. View Prescriptions to Fulfill");
            System.out.println("2. Update Prescription Status");
            System.out.println("3. View Medication Inventory");
            System.out.println("4. Submit Replenishment Request");
            System.out.println("5. Logout");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    viewPrescriptionsToFulfill(scanner);
                    break;
                case "2":
                    updatePrescriptionStatus(scanner);
                    break;
                case "3":
                    viewInventory();
                    break;
                case "4":
                    submitReplenishmentRequest(scanner);
                    break;
                case "5":
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Allows the pharmacist to view pending prescriptions.
     *
     * @param scanner the Scanner for user input
     */
    private void viewPrescriptionsToFulfill(Scanner scanner) {
        System.out.println("\n--- View Prescriptions to Fulfill ---");
        List<Appointment> appointments = appointmentCRUD.getAppointments(null, null, null);

        boolean found = false;
        for (Appointment appointment : appointments) {
            if (appointment.getOutcomeRecord() != null) {
                for (Prescription prescription : appointment.getOutcomeRecord().getPrescriptions()) {
                    if (prescription.getStatus() == PrescriptionStatus.PENDING) {
                        found = true;
                        System.out.printf("Appointment ID: %s, Patient ID: %s, Prescription: %s (Qty: %d, Status: %s)%n",
                                appointment.getAppointmentID(),
                                appointment.getPatientID(),
                                prescription.getMedicationName(),
                                prescription.getQuantity(),
                                prescription.getStatus());
                    }
                }
            }
        }

        if (!found) {
            System.out.println("No pending prescriptions found.");
        }
    }

    /**
     * Allows the pharmacist to update the status of prescriptions.
     *
     * @param scanner the Scanner for user input
     */
    private void updatePrescriptionStatus(Scanner scanner) {
        System.out.println("\n--- Update Prescription Status ---");

        // Retrieve all appointments with prescriptions
        List<Appointment> appointments = appointmentCRUD.getAppointments(null, null, null);

        int index = 1;
        List<Prescription> allPrescriptions = new ArrayList<>();
        List<Appointment> appointmentMapping = new ArrayList<>();

        // Display prescriptions from all appointments
        for (Appointment appointment : appointments) {
            if (appointment.getOutcomeRecord() != null) {
                for (Prescription prescription : appointment.getOutcomeRecord().getPrescriptions()) {
                    System.out.printf("%d. Appointment ID: %s, Patient ID: %s, Prescription: %s (Status: %s)%n",
                            index++, appointment.getAppointmentID(), appointment.getPatientID(),
                            prescription.getMedicationName(), prescription.getStatus());
                    allPrescriptions.add(prescription);
                    appointmentMapping.add(appointment);
                }
            }
        }

        // Check if any prescriptions exist
        if (allPrescriptions.isEmpty()) {
            System.out.println("No prescriptions found.");
            return;
        }

        // Prompt user to select a prescription
        System.out.print("Enter the number of the prescription to update: ");
        int choice = -1;

        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
            return;
        }

        // Validate selection
        if (choice < 1 || choice > allPrescriptions.size()) {
            System.out.println("Invalid choice. Please try again.");
            return;
        }

        // Update prescription status and persist changes
        Prescription selectedPrescription = allPrescriptions.get(choice - 1);
        Appointment associatedAppointment = appointmentMapping.get(choice - 1);

        selectedPrescription.updateStatus(PrescriptionStatus.DISPENSED);

        try {
            appointmentCRUD.updateAppointment(associatedAppointment);
            System.out.println("Prescription status updated to DISPENSED.");
        } catch (Exception e) {
            System.err.println("Error updating prescription status: " + e.getMessage());
        }
    }


    /**
     * Displays the inventory of medications.
     */
    private void viewInventory() {
        System.out.println("\n--- View Medication Inventory ---");
        medicineManager.displayInventory();
    }

    /**
     * Allows the pharmacist to submit a replenishment request.
     *
     * @param scanner the Scanner for user input
     */
    private void submitReplenishmentRequest(Scanner scanner) {
        try {
            replenishmentRequestManager.createRequest(scanner, pharmacist.getUserID());
        } catch (Exception e) {
            System.err.println("Error creating replenishment request: " + e.getMessage());
        }
    }
}

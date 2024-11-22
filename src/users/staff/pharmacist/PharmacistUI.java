package users.staff.pharmacist;

import datamgmt.retrievers.AppointmentData;
import datamgmt.retrievers.MedicineData;
import datamgmt.retrievers.PatientData;
import datamgmt.retrievers.ReplenishmentRequestData;
import utils.enums.PrescriptionStatus;
import utils.medicinemanagements.Medicine;
import utils.medicinemanagements.MedicineManager;
import utils.medicinemanagements.ReplenishmentRequestManager;
import utils.appointments.Appointment;
import utils.appointments.AppointmentCRUD;
import utils.medicalrecords.Prescription;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Provides the Pharmacist menu to manage prescriptions, inventory, and replenishment requests.
 */
public class PharmacistUI {

    private final MedicineManager medicineManager;
    private final ReplenishmentRequestManager replenishmentRequestManager;
    private final AppointmentCRUD appointmentCRUD;
    private final PatientData patientData;
    private final Pharmacist pharmacist;

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
        this.replenishmentRequestManager = new ReplenishmentRequestManager(requestData, medicineManager);
        this.appointmentCRUD = new AppointmentCRUD(appointmentData);
        this.patientData = new PatientData();
        this.patientData.importData();
        this.pharmacist = pharmacist;
    }

    /**
     * Displays the main menu for the pharmacist.
     */
    public void displayMenu() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n====================================");
            System.out.println("           PHARMACIST MENU          ");
            System.out.println("====================================");
            System.out.println("1. View Pending Prescriptions");
            System.out.println("2. Update Prescription Status");
            System.out.println("3. View Medication Inventory");
            System.out.println("4. Submit Replenishment Request");
            System.out.println("5. Logout");
            System.out.println("====================================");
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
     * Allows the pharmacist to view pending prescriptions, including the patient's name.
     *
     * @param scanner the Scanner for user input
     */
    private void viewPrescriptionsToFulfill(Scanner scanner) {
        System.out.println("\n====================================");
        System.out.println("        Pending Prescriptions       ");
        System.out.println("====================================");

        List<Appointment> appointments = appointmentCRUD.getAppointments(null, null, null);

        boolean found = false;
        int index = 1;
        for (Appointment appointment : appointments) {
            if (appointment.getOutcomeRecord() != null) {
                for (Prescription prescription : appointment.getOutcomeRecord().getPrescriptions()) {
                    if (prescription.getStatus() == PrescriptionStatus.PENDING) {
                        found = true;

                        String patientName = "Unknown";
                        if (patientData.findPatientById(appointment.getPatientID()) != null) {
                            patientName = patientData.findPatientById(appointment.getPatientID()).getName();
                        }

                        System.out.printf("%d. Appointment ID: %s%n", index++, appointment.getAppointmentID());
                        System.out.printf("   Patient Name  : %s (%s)%n", patientName, appointment.getPatientID());
                        System.out.printf("   Medication    : %s%n", prescription.getMedicationName());
                        System.out.printf("   Quantity      : %d%n", prescription.getQuantity());
                        System.out.printf("   Status        : %s%n", prescription.getStatus());
                        System.out.println("------------------------------------");
                    }
                }
            }
        }

        if (!found) {
            System.out.println("No pending prescriptions found.");
        }
    }

    /**
     * Allows the pharmacist to update the status of prescriptions, showing only pending prescriptions,
     * and updates the medicine stock information with the dispensed amount.
     *
     * @param scanner the Scanner for user input
     */
    private void updatePrescriptionStatus(Scanner scanner) {
        System.out.println("\n====================================");
        System.out.println("         Update Prescription        ");
        System.out.println("====================================");

        List<Appointment> appointments = appointmentCRUD.getAppointments(null, null, null);

        int index = 1;
        List<Prescription> pendingPrescriptions = new ArrayList<>();
        List<Appointment> appointmentMapping = new ArrayList<>();

        for (Appointment appointment : appointments) {
            if (appointment.getOutcomeRecord() != null) {
                for (Prescription prescription : appointment.getOutcomeRecord().getPrescriptions()) {
                    if (prescription.getStatus() == PrescriptionStatus.PENDING) {
                        System.out.printf("%d. Appointment ID: %s%n", index++, appointment.getAppointmentID());
                        System.out.printf("   Patient ID   : %s%n", appointment.getPatientID());
                        System.out.printf("   Medication   : %s%n", prescription.getMedicationName());
                        System.out.printf("   Quantity     : %d%n", prescription.getQuantity());
                        System.out.printf("   Status       : %s%n", prescription.getStatus());
                        System.out.println("------------------------------------");
                        pendingPrescriptions.add(prescription);
                        appointmentMapping.add(appointment);
                    }
                }
            }
        }

        if (pendingPrescriptions.isEmpty()) {
            System.out.println("No pending prescriptions found.");
            return;
        }

        System.out.print("Enter the number of the prescription to update: ");
        int choice = -1;

        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
            return;
        }

        if (choice < 1 || choice > pendingPrescriptions.size()) {
            System.out.println("Invalid choice. Please try again.");
            return;
        }

        Prescription selectedPrescription = pendingPrescriptions.get(choice - 1);
        Appointment associatedAppointment = appointmentMapping.get(choice - 1);

        // Update prescription status
        selectedPrescription.updateStatus(PrescriptionStatus.DISPENSED);

        try {
            appointmentCRUD.updateAppointment(associatedAppointment);
            System.out.println("Prescription status updated to DISPENSED.");

            // Update medicine stock
            String medicationName = selectedPrescription.getMedicationName();
            int quantityDispensed = selectedPrescription.getQuantity();

            Optional<Medicine> medicine = medicineManager.getMedicineData().findMedicineByName(medicationName);
            if (medicine.isPresent()) {
                Medicine updatedMedicine = medicine.get();
                updatedMedicine.reduceStock(quantityDispensed); // Ensure Medicine class has this method

                try {
                    medicineManager.getMedicineData().updateMedicine(updatedMedicine);
                    System.out.println("Medicine stock updated successfully.");
                } catch (Exception e) {
                    System.err.println("Error updating medicine stock: " + e.getMessage());
                }
            } else {
                System.out.println("Warning: Medicine not found in inventory. Stock update skipped.");
            }
        } catch (Exception e) {
            System.err.println("Error updating prescription status: " + e.getMessage());
        }
    }


    /**
     * Displays the inventory of medications.
     */
    private void viewInventory() {
        System.out.println("\n====================================");
        System.out.println("        Medication Inventory        ");
        System.out.println("====================================");
        medicineManager.displayInventory();
    }

    /**
     * Allows the pharmacist to submit a replenishment request.
     *
     * @param scanner the Scanner for user input
     */
    private void submitReplenishmentRequest(Scanner scanner) {
        System.out.println("\n====================================");
        System.out.println("     Submit Replenishment Request   ");
        System.out.println("====================================");
        try {
            replenishmentRequestManager.createRequest(scanner, pharmacist.getUserID());
        } catch (Exception e) {
            System.err.println("Error creating replenishment request: " + e.getMessage());
        }
    }
}

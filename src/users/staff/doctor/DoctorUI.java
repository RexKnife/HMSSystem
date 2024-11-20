package users.staff.doctor;

import datamgmt.retrievers.AppointmentData;
import datamgmt.retrievers.MedicalRecordData;
import utils.appointments.Appointment;
import utils.appointments.AppointmentCRUD;
import utils.medicalrecords.*;
import utils.enums.AppointmentStatus;
import users.ui.BaseUI;

import java.util.List;
import java.util.Scanner;

/**
 * UI for doctors to manage appointments and medical records.
 */
public class DoctorUI extends BaseUI {

    private final AppointmentCRUD appointmentCRUD;
    private final MedicalRecordData medicalRecordData;
    private final String doctorID;

    /**
     * Constructs the DoctorUI with necessary data handlers.
     *
     * @param doctorID the ID of the doctor using the UI
     */
    public DoctorUI(String doctorID) {
        AppointmentData appointmentData = new AppointmentData();
        appointmentData.importData();
        this.appointmentCRUD = new AppointmentCRUD(appointmentData);

        this.medicalRecordData = new MedicalRecordData();
        this.medicalRecordData.importData();

        this.doctorID = doctorID;
    }

    @Override
    public void displayMenu() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            displayMenuHeader("DOCTOR MAIN MENU");
            displayMenuOption(1, "View Medical Records");
            displayMenuOption(2, "Update Medical Records");
            displayMenuOption(3, "View Personal Schedule");
            displayMenuOption(4, "Accept/Decline Appointment Requests");
            displayMenuOption(5, "View Upcoming Appointments");
            displayMenuOption(6, "Record Appointment Outcome");
            displayMenuOption(7, "Logout");

            int choice = getMenuChoice();

            if (choice == 1) {
                viewMedicalRecords(scanner);
            } else if (choice == 2) {
                updateMedicalRecords(scanner);
            } else if (choice == 3) {
                viewPersonalSchedule();
            } else if (choice == 4) {
                manageAppointmentRequests(scanner);
            } else if (choice == 5) {
                viewUpcomingAppointments();
            } else if (choice == 6) {
                recordAppointmentOutcome(scanner);
            } else if (choice == 7) {
                System.out.println("Logging out...");
                return;
            } else {
                displayInvalidInputMessage();
            }
            pauseForUser();
        }
    }

    /**
     * Allows the doctor to view medical records of patients under their care.
     */
    private void viewMedicalRecords(Scanner scanner) {
        System.out.print("Enter Patient ID to view medical record: ");
        String patientID = scanner.nextLine().trim();

        MedicalRecord record = medicalRecordData.getMedicalRecordByPatientID(patientID);
        if (record != null) {
            System.out.println(record);
        } else {
            System.out.println("No medical record found for Patient ID: " + patientID);
        }
    }

    /**
     * Allows the doctor to update the medical records of patients.
     */
    private void updateMedicalRecords(Scanner scanner) {
        System.out.print("Enter Patient ID to update medical record: ");
        String patientID = scanner.nextLine().trim();

        MedicalRecord record = medicalRecordData.getMedicalRecordByPatientID(patientID);
        if (record == null) {
            System.out.println("No medical record found for Patient ID: " + patientID);
            return;
        }
        System.out.println("Existing Medical Record: " + record);

        System.out.print("Enter new diagnosis to add (or press Enter to skip): ");
        String diagnosis = scanner.nextLine().trim();
        if (!diagnosis.isEmpty()) {
            record.addDiagnosis(diagnosis);
        }

        System.out.print("Enter new treatment to add (or press Enter to skip): ");
        String treatment = scanner.nextLine().trim();
        if (!treatment.isEmpty()) {
            record.addTreatment(treatment);
        }

        try {
            medicalRecordData.updateMedicalRecord(record);
            System.out.println("Medical record updated successfully.");
        } catch (Exception e) {
            System.err.println("Error updating medical record: " + e.getMessage());
        }
    }

    /**
     * Allows the doctor to view their personal schedule of appointments.
     */
    private void viewPersonalSchedule() {
        List<Appointment> appointments = appointmentCRUD.getAppointments(null, doctorID, null);
        if (appointments.isEmpty()) {
            System.out.println("No scheduled appointments found.");
            return;
        }

        System.out.printf("%-15s %-15s %-15s %-15s%n", "Patient ID", "Date", "Time", "Status");
        for (Appointment appointment : appointments) {
            System.out.printf("%-15s %-15s %-15s %-15s%n",
                    appointment.getPatientID(),
                    appointment.getDate(),
                    appointment.getTime(),
                    appointment.getStatus());
        }
    }

    /**
     * Allows the doctor to accept or decline appointment requests.
     *
     * @param scanner the Scanner for user input
     */
    private void manageAppointmentRequests(Scanner scanner) {
        List<Appointment> requests = appointmentCRUD.getAppointments(null, doctorID, AppointmentStatus.PENDING);
        if (requests.isEmpty()) {
            System.out.println("No pending appointment requests.");
            return;
        }

        int index = 1;
        for (Appointment appointment : requests) {
            System.out.printf("%d. Patient: %s, Date: %s, Time: %s%n",
                    index++, appointment.getPatientID(), appointment.getDate(), appointment.getTime());
        }

        System.out.print("Enter the number of the appointment to manage (or 0 to exit): ");
        int choice = Integer.parseInt(scanner.nextLine().trim());

        if (choice == 0 || choice > requests.size()) {
            System.out.println("Invalid choice. Returning to main menu.");
            return;
        }

        Appointment selectedAppointment = requests.get(choice - 1);

        System.out.print("Accept or Decline this appointment? (accept/decline): ");
        String action = scanner.nextLine().trim().toLowerCase();

        if (action.equals("accept")) {
            selectedAppointment.updateStatus(AppointmentStatus.ACCEPTED);
            appointmentCRUD.updateAppointment(selectedAppointment);
            System.out.println("Appointment accepted.");
        } else if (action.equals("decline")) {
            selectedAppointment.updateStatus(AppointmentStatus.CANCELLED);
            appointmentCRUD.updateAppointment(selectedAppointment);
            System.out.println("Appointment declined.");
        } else {
            System.out.println("Invalid action. Returning to main menu.");
        }
    }

    /**
     * Allows the doctor to view a list of upcoming appointments.
     */
    private void viewUpcomingAppointments() {
        List<Appointment> upcomingAppointments = appointmentCRUD.getAppointments(null, doctorID, AppointmentStatus.ACCEPTED);
        if (upcomingAppointments.isEmpty()) {
            System.out.println("No upcoming appointments.");
            return;
        }

        System.out.printf("%-15s %-15s %-15s %-15s%n", "Patient ID", "Date", "Time", "Status");
        for (Appointment appointment : upcomingAppointments) {
            System.out.printf("%-15s %-15s %-15s %-15s%n",
                    appointment.getPatientID(),
                    appointment.getDate(),
                    appointment.getTime(),
                    appointment.getStatus());
        }
    }

    /**
     * Allows the doctor to record the outcome of completed appointments.
     *
     * @param scanner the Scanner for user input
     */
    private void recordAppointmentOutcome(Scanner scanner) {
        // Fetch completed appointments for the doctor
        List<Appointment> completedAppointments = appointmentCRUD.getAppointments(null, doctorID, AppointmentStatus.COMPLETED);
        if (completedAppointments.isEmpty()) {
            System.out.println("No completed appointments available for recording outcomes.");
            return;
        }
    
        // Display the completed appointments
        int index = 1;
        for (Appointment appointment : completedAppointments) {
            System.out.printf("%d. Patient: %s, Date: %s, Time: %s%n",
                    index++, appointment.getPatientID(), appointment.getDate(), appointment.getTime());
        }
    
        // Allow the doctor to select an appointment
        System.out.print("Enter the number of the appointment to record an outcome for (or 0 to exit): ");
        int choice = Integer.parseInt(scanner.nextLine().trim());
    
        if (choice == 0 || choice > completedAppointments.size()) {
            System.out.println("Invalid choice. Returning to main menu.");
            return;
        }
    
        Appointment selectedAppointment = completedAppointments.get(choice - 1);
    
        // Collect outcome details
        System.out.print("Enter type of service provided: ");
        String serviceType = scanner.nextLine().trim();
    
        System.out.print("Enter consultation notes: ");
        String consultationNotes = scanner.nextLine().trim();
    
        // Create the outcome record
        OutcomeRecord outcomeRecord = new OutcomeRecord(selectedAppointment.getDate(), serviceType, consultationNotes);
    
        // Collect prescription details (optional)
        System.out.print("Enter prescribed medication name (or press Enter to skip): ");
        String medicationName = scanner.nextLine().trim();
    
        if (!medicationName.isEmpty()) {
            System.out.print("Enter the quantity of the medication: ");
            int quantity = Integer.parseInt(scanner.nextLine().trim());
    
            // Create and add the prescription to the outcome record
            Prescription prescription = new Prescription(medicationName, quantity);
            outcomeRecord.addPrescription(prescription);
        }
    
        // Update the appointment with the outcome record
        selectedAppointment.setOutcomeRecord(outcomeRecord);
    
        try {
            appointmentCRUD.updateAppointment(selectedAppointment);
            System.out.println("Appointment outcome recorded successfully.");
        } catch (Exception e) {
            System.err.println("Error recording appointment outcome: " + e.getMessage());
        }
    }
    
}

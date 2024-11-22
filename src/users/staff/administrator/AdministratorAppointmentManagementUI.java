package users.staff.administrator;

import utils.appointments.AppointmentCRUD;
import datamgmt.retrievers.AppointmentData;
import datamgmt.retrievers.PatientData;
import datamgmt.retrievers.StaffData;
import utils.enums.AppointmentStatus;
import utils.appointments.Appointment;
import utils.medicalrecords.OutcomeRecord;
import users.patient.Patient;
import users.ui.BaseUI;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;

/**
 * Administrator UI for managing hospital appointments.
 * Provides functionality to view and manage scheduled appointments.
 */
public class AdministratorAppointmentManagementUI extends BaseUI {

    private final AppointmentCRUD appointmentCRUD;
    private Scanner scanner;
    /**
     * Constructs the `AdministratorAppointmentManagementUI` with the necessary dependencies.
     */
    public AdministratorAppointmentManagementUI(Scanner scanner) {
        AppointmentData appointmentData = new AppointmentData();
        appointmentData.reloadData();
        this.appointmentCRUD = new AppointmentCRUD(appointmentData);
        this.scanner = scanner;
    }

    public void displayMenu() {
        while (true) {
            displayMenuHeader("APPOINTMENT MANAGEMENT MENU");
            displayMenuOption(1, "View All Appointments");
            displayMenuOption(2, "Search Appointments");
            displayMenuOption(3, "Create and Send Receipt for a Completed Appointment");
            displayMenuOption(4, "Go Back to Main Menu");
    
            int choice = getMenuChoice();
    
            if (!isValidChoice(choice, 1, 4)) {
                displayInvalidInputMessage();
                pauseForUser(this.scanner);
                continue;
            }
    
            switch (choice) {
                case 1:
                    viewAllAppointments();
                    break;
                case 2:
                    searchAppointments(this.scanner);
                    break;
                case 3:
                    createAndSendReceipt(scanner);
                    break;
                case 4:
                    System.out.println("Returning to the main menu...");
                    return;
            }
            pauseForUser(this.scanner);
        }
    }
    

    /**
     * Displays all appointments in the system.
     */
    private void viewAllAppointments() {
        displayMenuHeader("ALL APPOINTMENTS");
        List<Appointment> appointments = appointmentCRUD.getAppointments(null, null, null);

        if (appointments.isEmpty()) {
            System.out.println("No appointments found.");
        } else {
            displayAppointmentDetails(appointments);
        }
    }

    /**
     * Searches for appointments based on filters.
     */
    private void searchAppointments(Scanner scanner) {
        displayMenuHeader("SEARCH APPOINTMENTS");

        System.out.print("Enter Patient ID (or press Enter to skip): ");
        String patientID = scanner.nextLine().trim();
        if (patientID.isEmpty()) patientID = null;

        System.out.print("Enter Doctor ID (or press Enter to skip): ");
        String doctorID = scanner.nextLine().trim();
        if (doctorID.isEmpty()) doctorID = null;

        System.out.print("Enter Appointment Status (ACCEPTED/CANCELED/COMPLETED/PENDING or press Enter to skip): ");
        String statusInput = scanner.nextLine().trim();
        AppointmentStatus status = statusInput.isEmpty() ? null : AppointmentStatus.valueOf(statusInput.toUpperCase());

        List<Appointment> appointments = appointmentCRUD.getAppointments(patientID, doctorID, status);

        if (appointments.isEmpty()) {
            System.out.println("No appointments found matching the criteria.");
        } else {
            displayAppointmentDetails(appointments);
        }
    }

    /**
     * Displays appointment details in a tabular format.
     *
     * @param appointments the list of appointments to display
     */
    private void displayAppointmentDetails(List<Appointment> appointments) {
        System.out.printf("%-12s %-10s %-10s %-12s %-8s %-20s%n",
                "AppointmentID", "PatientID", "DoctorID", "Status", "Date", "Time");
        System.out.println("----------------------------------------------------------------------------");

        for (Appointment appointment : appointments) {
            System.out.printf("%-12s %-10s %-10s %-12s %-8s %-20s%n",
                    appointment.getAppointmentID(),
                    appointment.getPatientID(),
                    appointment.getDoctorID(),
                    appointment.getStatus(),
                    appointment.getDate(),
                    appointment.getTime());
        }

        System.out.println("\nOUTCOME RECORDS FOR COMPLETED APPOINTMENTS:");
        for (Appointment appointment : appointments) {
            if (appointment.getStatus() == AppointmentStatus.COMPLETED && appointment.getOutcomeRecord() != null) {
                OutcomeRecord outcomeRecord = appointment.getOutcomeRecord();
                System.out.println("\nAppointment ID: " + appointment.getAppointmentID());
                System.out.println("Date of Outcome: " + outcomeRecord.getDateOfAppointment());
                System.out.println("Service Type: " + outcomeRecord.getServiceType());
                System.out.println("Consultation Notes: " + outcomeRecord.getConsultationNotes());
                System.out.println("Prescriptions: " + outcomeRecord.getPrescriptions());
            }
        }
    }

    private void createAndSendReceipt(Scanner scanner) {
        displayMenuHeader("CREATE AND SEND RECEIPT");

        try {
            // Fetch all completed appointments
            List<Appointment> completedAppointments = appointmentCRUD.getAppointments(null, null, AppointmentStatus.COMPLETED);
            if (completedAppointments.isEmpty()) {
                System.out.println("No completed appointments available for receipt generation.");
                return;
            }

            // Display completed appointments
            System.out.println("Completed Appointments:");
            for (int i = 0; i < completedAppointments.size(); i++) {
                Appointment appointment = completedAppointments.get(i);
                System.out.printf("%d. Appointment ID: %s, Patient ID: %s, Doctor ID: %s, Date: %s, Time: %s%n",
                    i + 1,
                    appointment.getAppointmentID(),
                    appointment.getPatientID(),
                    appointment.getDoctorID(),
                    appointment.getDate(),
                    appointment.getTime());
            }

            // Select appointment
            System.out.print("Select an appointment by number: ");
            int choice = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (choice < 0 || choice >= completedAppointments.size()) {
                System.out.println("Invalid selection. Operation canceled.");
                return;
            }

            Appointment selectedAppointment = completedAppointments.get(choice);

            // Fetch patient details
            PatientData patientData = new PatientData();
            patientData.reloadData();
            Patient patient = patientData.findPatientById(selectedAppointment.getPatientID());
            System.out.println(selectedAppointment);
            if (patient == null) {
                System.out.println("Patient not found. Operation canceled.");
                return;
            }

            String patientEmail = patient.getContactInfo();

            // Confirm or update email
            System.out.println("Stored email for patient: " + patientEmail);
            System.out.print("Is this the correct email? (yes to confirm, no to update): ");
            String emailConfirmation = scanner.nextLine().trim().toLowerCase();

            if (emailConfirmation.equals("no")) {
                System.out.print("Enter new email address: ");
                patientEmail = scanner.nextLine().trim();
                if (!patientEmail.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
                    System.out.println("Invalid email format. Operation canceled.");
                    return;
                }
            }

            // Prompt for receipt details
            System.out.print("Enter Total Amount: ");
            double totalAmount = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("Enter Payment Method (e.g., Cash, Card): ");
            String paymentMethod = scanner.nextLine().trim();
            StaffData staffData = new StaffData();
            staffData.importData();
            // Python script execution
            createAndSendReceiptWithPython(
                selectedAppointment.getAppointmentID(),
                patient.getName(),
                patientEmail,
                totalAmount,
                paymentMethod,
                selectedAppointment.getDate(),
                selectedAppointment.getTime(),
                "Dr. " + (staffData.findUserById(selectedAppointment.getDoctorID())).getName(), // Replace with actual doctor name retrieval logic
                "General Consultation" // Replace with specific service type
            );

            System.out.println("Receipt created and sent to: " + patientEmail);

        } catch (Exception e) {
            System.err.println("Error generating or sending receipt: " + e.getMessage());
        }
    }
    private void createAndSendReceiptWithPython(
        String appointmentID,
        String patientName,
        String patientEmail,
        double totalAmount,
        String paymentMethod,
        String appointmentDate,
        String appointmentTime,
        String doctorName,
        String serviceType) {
        try {
            // Prepare the Python command
            String pythonCommand = "python"; // Adjust to "python3" if required on your system
            String pythonScriptPath = "src/users/staff/administrator/generate_receipt.py"; // Provide the correct path to the Python script

            // Build the process
            ProcessBuilder processBuilder = new ProcessBuilder(
                pythonCommand,
                pythonScriptPath,
                appointmentID,
                patientName,
                patientEmail,
                String.valueOf(totalAmount),
                paymentMethod,
                appointmentDate,
                appointmentTime,
                doctorName,
                serviceType
            );

            // Start the process
            Process process = processBuilder.start();

            // Capture output and errors
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            // Print script output
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Print script errors (if any)
            while ((line = errorReader.readLine()) != null) {
                System.err.println(line);
            }

            // Wait for the process to finish
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Receipt sent successfully via Python script.");
            } else {
                System.err.println("Error occurred while executing Python script.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to execute Python script: " + e.getMessage());
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

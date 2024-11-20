package users.staff.administrator;

import utils.appointments.AppointmentCRUD;
import datamgmt.retrievers.AppointmentData;
import utils.enums.AppointmentStatus;
import utils.appointments.Appointment;
import utils.medicalrecords.OutcomeRecord;
import users.ui.BaseUI;

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

    @Override
    public void displayMenu() {
        while (true) {
            displayMenuHeader("APPOINTMENT MANAGEMENT MENU");
            displayMenuOption(1, "View All Appointments");
            displayMenuOption(2, "Search Appointments");
            displayMenuOption(3, "Go Back to Main Menu");

            int choice = getMenuChoice();

            if (!isValidChoice(choice, 1, 3)) {
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

    /**
     * Prompts the user to press Enter to continue.
     */
    private void pauseForUser(Scanner scanner) {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
}
